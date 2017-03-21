package com.jso.tagit.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.apphosting.datastore.DatastoreV4;
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
        name = "fishermanApi",
        version = "v6",
        resource = "fisherman",
        clientIds = {FishermanEndpoint.Constants.WEB_CLIENT_ID, FishermanEndpoint.Constants.ANDROID_CLIENT_ID},
        audiences = {FishermanEndpoint.Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(
                ownerDomain = "backend.tagit.jso.com",
                ownerName = "backend.tagit.jso.com",
                packagePath = ""
        )
)


public class FishermanEndpoint {

    private static final Logger logger = Logger.getLogger(FishermanEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 1000;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Fisherman.class);
    }

    /**
     * Returns the {@link Fisherman} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Fisherman} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "fisherman/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Fisherman get(@Named("id") String id) throws NotFoundException {
        logger.info("Getting Fisherman with ID: " + id);
        Fisherman fisherman = ofy().load().type(Fisherman.class).id(id).now();
        if (fisherman == null) {
            throw new NotFoundException("Could not find Fisherman with ID: " + id);
        }
        return fisherman;
    }

    /**
     * Inserts a new {@code Fisherman}.
     */
    @ApiMethod(
            name = "insert",
            path = "fisherman",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Fisherman insert(Fisherman fisherman) throws NotFoundException {
        fisherman.setLast_modified(new Date().getTime());
        ofy().save().entity(fisherman).now();
        logger.info("Created Fisherman with ID: " + fisherman.getId());

        return ofy().load().entity(fisherman).now();
    }

    /**
     * Updates an existing {@code Fisherman}.
     *
     * @param id        the ID of the entity to be updated
     * @param fisherman the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Fisherman}
     */
    @ApiMethod(
            name = "update",
            path = "fisherman/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Fisherman update(@Named("id") String id, Fisherman fisherman) throws NotFoundException {
        fisherman.setLast_modified(new Date().getTime());
        ofy().save().entity(fisherman).now();
        logger.info("Updated Fisherman: " + fisherman);
        return ofy().load().entity(fisherman).now();
    }

    /**
     * Deletes the specified {@code Fisherman}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Fisherman}
     */
    @ApiMethod(
            name = "remove",
            path = "fisherman/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") String id) throws NotFoundException {
        if (checkExists(id)) {
            ofy().delete().type(Fisherman.class).id(id).now();
            logger.info("Deleted Fisherman with ID: " + id);
        }
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
            path = "fisherman/all/{userid}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Fisherman> list(@Named("userid") String userid, @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Fisherman> query = ofy().load().type(Fisherman.class).filter("userid = ", userid).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Fisherman> queryIterator = query.iterator();
        List<Fisherman> fishermanList = new ArrayList<Fisherman>(limit);
        while (queryIterator.hasNext()) {
            fishermanList.add(queryIterator.next());
        }
        return CollectionResponse.<Fisherman>builder().setItems(fishermanList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "getModified",
            path = "fisherman/sync/{userid}/{timestamp}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Fisherman> getModified(@Named("userid") String userid, @Named("timestamp") long timestamp, @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<Fisherman> query = ofy().load()
                .type(Fisherman.class)
                .filter(FilterHelper.getSyncFilter(userid, timestamp))
                .limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Fisherman> queryIterator = query.iterator();
        List<Fisherman> fishermanList = new ArrayList<Fisherman>(limit);
        while (queryIterator.hasNext()) {
            fishermanList.add(queryIterator.next());
        }
        return CollectionResponse.<Fisherman>builder().setItems(fishermanList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private boolean checkExists(String id) {
        try {
            ofy().load().type(Fisherman.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            return false;
        }

        return true;
    }

    public class Constants {
        public static final String WEB_CLIENT_ID = "47651152390-ojgfrgf9mblke3b74d33ci92n8fluuj8.apps.googleusercontent.com";
        public static final String ANDROID_CLIENT_ID = "47651152390-70465t5e45tmc8m2fiaeld2ae9bkjco0.apps.googleusercontent.com";
        public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;

        public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    }

}