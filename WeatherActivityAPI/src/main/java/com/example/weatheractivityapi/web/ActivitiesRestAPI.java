package com.example.weatheractivityapi.web;

import com.example.weatheractivityapi.model.Activity;
import com.example.weatheractivityapi.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Component
@Path("/activities")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ActivitiesRestAPI {

    @Autowired
    private ActivityService service;

    @GET
    public Response getAll() {
        try {
            List<Activity> activities = service.getAll();
            return Response.ok(activities).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la récupération des activités: " + e.getMessage())
                    .build();
        }
    }

    @POST
    public Response create(Activity activity) {
        try {
            Activity savedActivity = service.save(activity);
            return Response.status(Response.Status.CREATED).entity(savedActivity).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur lors de la création: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        try {
            Activity activity = service.getById(id);
            if (activity != null) {
                return Response.ok(activity).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Activité non trouvée avec l'ID: " + id)
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la récupération: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/by-tags")
    public Response getByTags(@QueryParam("tags") List<String> tags) {
        try {
            List<Activity> activities = service.getByTags(tags);
            return Response.ok(activities).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la recherche par tags: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/search")
    public Response searchActivities(@QueryParam("param") String param) {
        try {
            List<Activity> activities = service.searchByTitle(param);
            return Response.ok(activities).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la recherche: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        try {
            Activity activity = service.getById(id);
            if (activity != null) {
                service.deleteById(id);
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Activité non trouvée avec l'ID: " + id)
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la suppression: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Activity activity) {
        try {
            Activity existingActivity = service.getById(id);
            if (existingActivity != null) {
                activity.setId(id);
                Activity updatedActivity = service.save(activity);
                return Response.ok(updatedActivity).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Activité non trouvée avec l'ID: " + id)
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur lors de la mise à jour: " + e.getMessage())
                    .build();
        }
    }
}