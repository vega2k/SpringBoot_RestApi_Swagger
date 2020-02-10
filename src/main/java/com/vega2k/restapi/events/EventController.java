package com.vega2k.restapi.events;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import java.net.URI;
import java.util.Optional;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.vega2k.restapi.accounts.Account;
import com.vega2k.restapi.accounts.AccountAdapter;
import com.vega2k.restapi.accounts.CurrentUser;
import com.vega2k.restapi.common.ErrorsResource;

@Controller
@RequestMapping(value="/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {
	//Spring4.3 부터는 @Autopwired 대신에   
	//변수를 선언하고, 생성자를 선언해서 대체할 수 있다.  
	private final EventRepository eventRepository;
	
	private final ModelMapper modelMapper;
	
	private final EventValidator eventValidator;
	
	public EventController(EventRepository eventRepository, 
			ModelMapper modelMapper, EventValidator eventValidator) {
		this.eventRepository = eventRepository;
		this.modelMapper = modelMapper;
		this.eventValidator = eventValidator;
	}
	
	//@PostMapping("/api/events")
	@PostMapping
//	public ResponseEntity createEvent(@RequestBody Event event) {
	public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, 
			Errors errors, @CurrentUser Account currentUser) {		
		
		/*
		 * Event event = Event.builder()
		 *               .name(eventDto.getName())
		 *               .description(eventDto.getDescription())
		 *               .build();
		 */
		
		if(errors.hasErrors()) {
			//return ResponseEntity.badRequest().build();
			//return ResponseEntity.badRequest().body(errors);
			return badRequest(errors);
		}
		
		eventValidator.validate(eventDto, errors);
		if(errors.hasErrors()) {
			//return ResponseEntity.badRequest().build();
			//return ResponseEntity.badRequest().body(errors);
			return badRequest(errors);
		}
		
		Event event = modelMapper.map(eventDto, Event.class);		
		event.update();
		event.setManager(currentUser);
		
		Event addEvent = this.eventRepository.save(event);
		
		//URI createUri = linkTo(methodOn(EventController.class).createEvent()).slash("{id}").toUri();
		ControllerLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(addEvent.getId());
		URI createUri = selfLinkBuilder.toUri();
		System.out.println(addEvent);
		
		EventResource eventResource = new EventResource(event);
		eventResource.add(linkTo(EventController.class).withRel("query-events"));
		//eventResource.add(selfLinkBuilder.withSelfRel());
		eventResource.add(selfLinkBuilder.withRel("update-event"));
		
		//return ResponseEntity.created(createUri).build();
		//return ResponseEntity.created(createUri).body(event);
		return ResponseEntity.created(createUri).body(eventResource);
	}

	private ResponseEntity badRequest(Errors errors) {
		return ResponseEntity.badRequest().body(new ErrorsResource(errors));
	}

	@GetMapping("/{id}")
	public ResponseEntity getEvent(@PathVariable Integer id, @CurrentUser Account currentUser) {
		Optional<Event> optionalEvent = this.eventRepository.findById(id);
		if(optionalEvent.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		Event event = optionalEvent.get();
		EventResource eventResource = new EventResource(event);
		
		//현재 사용자가 Event를 등록한 사용자와 동일하면 
		if((event.getManager() != null) && (event.getManager().equals(currentUser))) {
			eventResource.add(linkTo(EventController.class).slash(event.getId()).withRel("update-event"));
		}
		return ResponseEntity.ok(eventResource);
	}
	
	@PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id,
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        this.eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Event existingEvent = optionalEvent.get();
        System.out.println("==> existingEvent.getManager() " + existingEvent.getManager());
        System.out.println("==> currentUser " + currentUser );
        //로그인한 사용자가 이 Event를 등록한 사용자가 아니면 인가되지 않았다는 에러를 발생시킨다.
        if((existingEvent.getManager() != null) && (!existingEvent.getManager().equals(currentUser))) {
        	return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        
        System.out.println("DB 있는 Event : " + existingEvent);
        System.out.println("입력 받은 Event " + eventDto);
        this.modelMapper.map(eventDto, existingEvent);
        Event savedEvent = this.eventRepository.save(existingEvent);

        EventResource eventResource = new EventResource(savedEvent);

        return ResponseEntity.ok(eventResource);

	}
	
	//3단계
	@GetMapping
	public ResponseEntity queryEvents(Pageable pageable, 
			PagedResourcesAssembler<Event> assembler, 
			//@AuthenticationPrincipal User user
			//@AuthenticationPrincipal AccountAdapter currentUser) {
		@CurrentUser Account account) {
		//Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		//User principal = (User)authentication.getPrincipal();	
		
		Page<Event> page = this.eventRepository.findAll(pageable);
		PagedResources<Resource<Event>> pagedResources = 
				assembler.toResource(page, event -> new EventResource(event));
		System.out.println("<<<=======>>> " + account);
		if(account != null) {
			pagedResources.add(linkTo(EventController.class).withRel("create-event"));
		}
		return ResponseEntity.ok(pagedResources);
	}

//2단계	
//	public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
//		Page<Event> page = this.eventRepository.findAll(pageable);
//		PagedResources<Resource<Event>> pagedResources = assembler.toResource(page);
//		return ResponseEntity.ok(pagedResources);
//	}

//1단계	
//	public ResponseEntity queryEvents(Pageable pageable) {
//		return ResponseEntity.ok(this.eventRepository.findAll(pageable));
//	}

}
