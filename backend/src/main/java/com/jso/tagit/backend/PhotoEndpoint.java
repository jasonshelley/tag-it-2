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
        name = "photoApi",
        version = "v2",
        resource = "photo",
        namespace = @ApiNamespace(
                ownerDomain = "backend.tagit.jso.com",
                ownerName = "backend.tagit.jso.com",
                packagePath = ""
        )
)
public class PhotoEndpoint {

    private static final Logger logger = Logger.getLogger(PhotoEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Photo.class);
    }

    /**
     * Returns the {@link Photo} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Photo} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "photo/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Photo get(@Named("id") String id) throws NotFoundException {
        logger.info("Getting Photo with ID: " + id);
        Photo photo = ofy().load().type(Photo.class).id(id).now();
        if (photo == null) {
            throw new NotFoundException("Could not find Photo with ID: " + id);
        }
        return photo;
    }

    @ApiMethod(
            name = "getByFishId",
            path = "photo/fish/{fishid}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Photo getByFishId(@Named("fishid") String fishid) throws NotFoundException {
        logger.info("Getting Photo with fish ID: " + fishid);
        com.google.appengine.api.datastore.Query.Filter fishIdFilter =
                new com.google.appengine.api.datastore.Query.FilterPredicate("fishid",
                        com.google.appengine.api.datastore.Query.FilterOperator.EQUAL,
                        fishid);

        Query<Photo> query = ofy().load().type(Photo.class).filter(fishIdFilter).limit(1);
        Photo photo = query.iterator().next();
        if (photo == null) {
            throw new NotFoundException("Could not find Photo with fish ID: " + fishid);
        }
        return photo;
    }

    /**
     * Inserts a new {@code Photo}.
     */
    @ApiMethod(
            name = "insert",
            path = "photo",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Photo insert(Photo photo) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that photo.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        photo.setLast_modified(new Date().getTime());
        ofy().save().entity(photo).now();
        logger.info("Created Photo with ID: " + photo.getId());

        return ofy().load().entity(photo).now();
    }

    /**
     * Updates an existing {@code Photo}.
     *
     * @param id    the ID of the entity to be updated
     * @param photo the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Photo}
     */
    @ApiMethod(
            name = "update",
            path = "photo/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Photo update(@Named("id") String id, Photo photo) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        photo.setLast_modified(new Date().getTime());
        ofy().save().entity(photo).now();
        logger.info("Updated Photo: " + photo);
        return ofy().load().entity(photo).now();
    }

    /**
     * Deletes the specified {@code Photo}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Photo}
     */
    @ApiMethod(
            name = "remove",
            path = "photo/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") String id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Photo.class).id(id).now();
        logger.info("Deleted Photo with ID: " + id);
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
            path = "photo",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Photo> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Photo> query = ofy().load().type(Photo.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Photo> queryIterator = query.iterator();
        List<Photo> photoList = new ArrayList<Photo>(limit);
        while (queryIterator.hasNext()) {
            photoList.add(queryIterator.next());
        }
        return CollectionResponse.<Photo>builder().setItems(photoList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "getModified",
            path = "photo/sync/{userid}/{timestamp}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Photo> getModified(@Named("userid") String userid, @Named("timestamp") long timestamp, @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<Photo> query = ofy().load()
                .type(Photo.class)
                .filter(FilterHelper.getSyncFilter(userid, timestamp))
                .limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Photo> queryIterator = query.iterator();
        List<Photo> photoList = new ArrayList<Photo>(limit);
        while (queryIterator.hasNext()) {
            photoList.add(queryIterator.next());
        }
        return CollectionResponse.<Photo>builder().setItems(photoList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(String id) throws NotFoundException {
        try {
            ofy().load().type(Photo.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Photo with ID: " + id);
        }
    }
}