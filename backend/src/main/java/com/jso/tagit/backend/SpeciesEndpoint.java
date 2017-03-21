package com.jso.tagit.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "speciesApi",
        version = "v1",
        resource = "species",
        namespace = @ApiNamespace(
                ownerDomain = "backend.tagit.jso.com",
                ownerName = "backend.tagit.jso.com",
                packagePath = ""
        )
)
public class SpeciesEndpoint {

    private static final Logger logger = Logger.getLogger(SpeciesEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 1500;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Species.class);
    }

    /**
     * Returns the {@link Species} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Species} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "species/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Species get(@Named("id") String id) throws NotFoundException {
        logger.info("Getting Species with ID: " + id);
        Species species = ofy().load().type(Species.class).id(id).now();
        if (species == null) {
            throw new NotFoundException("Could not find Species with ID: " + id);
        }
        return species;
    }

    /**
     * Inserts a new {@code Species}.
     */
    @ApiMethod(
            name = "insert",
            path = "species",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Species insert(Species species) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that species.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        species.setLast_modified(new Date().getTime());
        ofy().save().entity(species).now();
        logger.info("Created Species with ID: " + species.getId());

        return ofy().load().entity(species).now();
    }

    /**
     * Updates an existing {@code Species}.
     *
     * @param id      the ID of the entity to be updated
     * @param species the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Species}
     */
    @ApiMethod(
            name = "update",
            path = "species/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Species update(@Named("id") String id, Species species) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        species.setLast_modified(new Date().getTime());
        ofy().save().entity(species).now();
        logger.info("Updated Species: " + species);
        return ofy().load().entity(species).now();
    }

    /**
     * Deletes the specified {@code Species}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Species}
     */
    @ApiMethod(
            name = "remove",
            path = "species/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") String id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Species.class).id(id).now();
        logger.info("Deleted Species with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "list",
            path = "species",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Species> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Species> query = ofy().load().type(Species.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Species> queryIterator = query.iterator();
        List<Species> speciesList = new ArrayList<Species>(limit);
        while (queryIterator.hasNext()) {
            speciesList.add(queryIterator.next());
        }
        return CollectionResponse.<Species>builder().setItems(speciesList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "getModified",
            path = "species/sync/{timestamp}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Species> getModified(@Named("timestamp") long timestamp, @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<Species> query = ofy().load()
                .type(Species.class)
                .filter(FilterHelper.getSyncFilter(timestamp))
                .limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Species> queryIterator = query.iterator();
        List<Species> speciesList = new ArrayList<Species>(limit);
        while (queryIterator.hasNext()) {
            speciesList.add(queryIterator.next());
        }
        return CollectionResponse.<Species>builder().setItems(speciesList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }


    private void checkExists(String id) throws NotFoundException {
        try {
            ofy().load().type(Species.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Species with ID: " + id);
        }
    }
}