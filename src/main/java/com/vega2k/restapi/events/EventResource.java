package com.vega2k.restapi.events;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

//기본적으로 BeanSerializer를 사용합니다. 
//public class EventResource extends ResourceSupport {
public class EventResource extends Resource<Event> {

	public EventResource(Event event, Link... links) {
		super(event, links);
		//add(new Link("http://localhost:8080/api/events" + event.getId())) 와 동일한 것임
		add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
	}

//	@JsonUnwrapped
//	private Event event;
//	
//	public EventResource(Event event) {
//		this.event = event;
//	}
//	
//	public Event getEvent() {
//		return event;
//	}
}
