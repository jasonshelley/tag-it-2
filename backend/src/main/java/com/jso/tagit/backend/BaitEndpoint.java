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
        name = "baitApi",
        version = "v1",
        resource = "bait",
        namespace = @ApiNamespace(
                ownerDomain = "backend.tagit.jso.com",
                ownerName = "backend.tagit.jso.com",
                packagePath = ""
        )
)
public class BaitEndpoint {

    private static final Logger logger = Logger.getLogger(BaitEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 1000;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Bait.class);
    }

    /**
     * Returns the {@link Bait} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Bait} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "bait/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Bait get(@Named("id") String id) throws NotFoundException {
        logger.info("Getting Bait with ID: " + id);
        Bait bait = ofy().load().type(Bait.class).id(id).now();
        if (bait == null) {
            throw new NotFoundException("Could not find Bait with ID: " + id);
        }
        return bait;
    }

    /**
     * Inserts a new {@code Bait}.
     */
    @ApiMethod(
            name = "insert",
            path = "bait",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Bait insert(Bait bait) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that bait.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        bait.setLast_modified(new Date().getTime());
        ofy().save().entity(bait).now();
        logger.info("Created Bait with ID: " + bait.getId());

        return ofy().load().entity(bait).now();
    }

    /**
     * Updates an existing {@code Bait}.
     *
     * @param id   the ID of the entity to be updated
     * @param bait the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Bait}
     */
    @ApiMethod(
            name = "update",
            path = "bait/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Bait update(@Named("id") String id, Bait bait) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        bait.setLast_modified(new Date().getTime());
        ofy().save().entity(bait).now();
        logger.info("Updated Bait: " + bait);
        return ofy().load().entity(bait).now();
    }



    /**
     * Deletes the specified {@code Bait}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Bait}
     */
    @ApiMethod(
            name = "remove",
            path = "bait/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") String id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Bait.class).id(id).now();
        logger.info("Deleted Bait with ID: " + id);
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
            path = "bait",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Bait> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Bait> query = ofy().load().type(Bait.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Bait> queryIterator = query.iterator();
        List<Bait> baitList = new ArrayList<Bait>(limit);
        while (queryIterator.hasNext()) {
            baitList.add(queryIterator.next());
        }
        return CollectionResponse.<Bait>builder().setItems(baitList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "getModified",
            path = "bait/sync/{timestamp}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Bait> getModified(@Named("timestamp") long timestamp, @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<Bait> query = ofy().load()
                .type(Bait.class)
                .filter(FilterHelper.getSyncFilter(timestamp))
                .limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Bait> queryIterator = query.iterator();
        List<Bait> baitList = new ArrayList<Bait>(limit);
        while (queryIterator.hasNext()) {
            baitList.add(queryIterator.next());
        }
        return CollectionResponse.<Bait>builder().setItems(baitList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }


    private void checkExists(String id) throws NotFoundException {
        try {
            ofy().load().type(Bait.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Bait with ID: " + id);
        }
    }
}