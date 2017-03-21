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
        name = "fishApi",
        version = "v1",
        resource = "fish",
        namespace = @ApiNamespace(
                ownerDomain = "backend.tagit.jso.com",
                ownerName = "backend.tagit.jso.com",
                packagePath = ""
        )
)
public class FishEndpoint {

    private static final Logger logger = Logger.getLogger(FishEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 500;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Fish.class);
    }

    /**
     * Returns the {@link Fish} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Fish} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "fish/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Fish get(@Named("id") String id) throws NotFoundException {
        logger.info("Getting Fish with ID: " + id);
        Fish fish = ofy().load().type(Fish.class).id(id).now();
        if (fish == null) {
            throw new NotFoundException("Could not find Fish with ID: " + id);
        }
        return fish;
    }

    /**
     * Inserts a new {@code Fish}.
     */
    @ApiMethod(
            name = "insert",
            path = "fish",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Fish insert(Fish fish) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that fish.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        fish.setLast_modified(new Date().getTime());
        ofy().save().entity(fish).now();
        logger.info("Created Fish with ID: " + fish.getId());

        return ofy().load().entity(fish).now();
    }

    /**
     * Updates an existing {@code Fish}.
     *
     * @param id   the ID of the entity to be updated
     * @param fish the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Fish}
     */
    @ApiMethod(
            name = "update",
            path = "fish/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Fish update(@Named("id") String id, Fish fish) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        fish.setLast_modified(new Date().getTime());
        ofy().save().entity(fish).now();
        logger.info("Updated Fish: " + fish);
        return ofy().load().entity(fish).now();
    }

    /**
     * Deletes the specified {@code Fish}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Fish}
     */
    @ApiMethod(
            name = "remove",
            path = "fish/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") String id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Fish.class).id(id).now();
        logger.info("Deleted Fish with ID: " + id);
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
            path = "fish",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Fish> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Fish> query = ofy().load().type(Fish.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Fish> queryIterator = query.iterator();
        List<Fish> fishList = new ArrayList<Fish>(limit);
        while (queryIterator.hasNext()) {
            fishList.add(queryIterator.next());
        }
        return CollectionResponse.<Fish>builder().setItems(fishList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "getModified",
            path = "fish/sync/{userid}/{timestamp}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Fish> getModified(@Named("userid") String userid, @Named("timestamp") long timestamp, @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<Fish> query = ofy().load()
                .type(Fish.class)
                .filter(FilterHelper.getSyncFilter(userid, timestamp))
                .limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Fish> queryIterator = query.iterator();
        List<Fish> fishList = new ArrayList<Fish>(limit);
        while (queryIterator.hasNext()) {
            fishList.add(queryIterator.next());
        }
        return CollectionResponse.<Fish>builder().setItems(fishList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(String id) throws NotFoundException {
        try {
            ofy().load().type(Fish.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Fish with ID: " + id);
        }
    }
}