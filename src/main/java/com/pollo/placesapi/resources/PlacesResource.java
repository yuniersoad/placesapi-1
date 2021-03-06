package com.pollo.placesapi.resources;

import com.pollo.placesapi.persistence.PlacesRepository;
import com.pollo.placesapi.persistence.model.Location;
import com.pollo.placesapi.persistence.model.Place;
import com.pollo.placesapi.persistence.model.Review;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.*;

@Path("/places")
@Produces(MediaType.APPLICATION_JSON)
public class PlacesResource {

	private final PlacesRepository repository;

	public PlacesResource(PlacesRepository repository){
		this.repository = repository;
	}

	@GET
	public Response getPlaces(){
		return Response.ok(repository.list()).build();
	}

	@POST
	public Response addPlace(@NotNull @FormParam("name") String name,
							 @NotNull @Max(value= 90) @Min(value= -90) @FormParam("lat") Double lat,
							 @NotNull @Max(value= 180) @Min(value=-108) @FormParam("lng") Double lng){

		final String id = UUID.randomUUID().toString();
		final Location location = new Location(lat, lng);

		final Place place = new Place(id, name, location);
		repository.add(place);
		return Response.status(Status.CREATED).entity(place).build();
	}


	@DELETE
	@Path("{id}")
	public Response deletePlace(@PathParam("id") final String id){
		return repository.find(id).map(place -> {
			repository.remove(id);
			return Response.status(Status.NO_CONTENT).build();
		}).orElse(Response.status(Status.NOT_FOUND).build());
	}

	@POST
	@Path("{id}/reviews")
	public Response addReview(@PathParam("id") final String id,
							  @NotNull @Max(value= 5) @Min(value=1) @FormParam("score") Integer score,
							  @FormParam("comment") String comment){
		return repository.find(id).map(place -> {
			final Review review = new Review(score, comment);
			place.addReview(review);
			return Response.status(Status.CREATED).entity(review).build();
		}).orElse(Response.status(Status.NOT_FOUND).build());
	}

	@GET
	@Path("{id}/reviews")
	public Response getReviews(@PathParam("id") final String id){
		return repository.find(id).map(place -> Response.status(Status.OK).entity(place.getReviews()).build())
				.orElse(Response.status(Status.NOT_FOUND).entity("{" +
				"    \"errors\": [" +
				"        \"place not found\"" +
				"    ]" +
				"}").build());
	}
}
