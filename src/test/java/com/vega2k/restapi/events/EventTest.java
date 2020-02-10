package com.vega2k.restapi.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EventTest {
	@Test
	public void builder() {
		Event event = Event.builder()
					.name("Spring REST API")
					.description("REST API developmemt with Spring")
					.build();
		assertThat(event).isNotNull();
	}
	
	@Test
	public void javaBean() {
		//Give
		String name = "Event";
		String description  = "Spring";
		
		//When
		Event event = new Event();
		event.setName(name);
		event.setDescription(description);
		
		//Then
		assertThat(event.getName()).isEqualTo("Event");
		assertThat(event.getDescription()).isEqualTo("Spring");
	}
}
