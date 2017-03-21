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
        name = "poleApi",
        version = "v1",
        resource = "pole",
        namespace = @ApiNamespace(
                ownerDomain = "backend.tagit.jso.com",
                ownerName = "backend.tagit.jso.com",
                packagePath = ""
        )
)
public class PoleEndpoint {

    private static final Logger logger = Logger.getLogger(PoleEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Pole.class);
    }

    /**
     * Returns the {@link Pole} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Pole} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "pole/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Pole get(@Named("id") String id) throws NotFoundException {
        logger.info("Getting Pole with ID: " + id);
        Pole pole = ofy().load().type(Pole.class).id(id).now();
        if (pole == null) {
            throw new NotFoundException("Could not find Pole with ID: " + id);
        }
        return pole;
    }

    /**
     * Inserts a new {@code Pole}.
     */
    @ApiMethod(
            name = "insert",
            path = "pole",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Pole insert(Pole pole) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that pole.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        pole.setLast_modified(new Date().getTime());
        ofy().save().entity(pole).now();
        logger.info("Created Pole with ID: " + pole.getId());

        return ofy().load().entity(pole).now();
    }

    /**
     * Updates an existing {@code Pole}.
     *
     * @param id   the ID of the entity to be updated
     * @param pole the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Pole}
     */
    @ApiMethod(
            name = "update",
            path = "pole/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Pole update(@Named("id") String id, Pole pole) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        pole.setLast_modified(new Date().getTime());
        ofy().save().entity(pole).now();
        logger.info("Updated Pole: " + pole);
        return ofy().load().entity(pole).now();
    }

    /**
     * Deletes the specified {@code Pole}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Pole}
     */
    @ApiMethod(
            name = "remove",
            path = "pole/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") String id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Pole.class).id(id).now();
        logger.info("Deleted Pole with ID: " + id);
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
            path = "pole",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Pole> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Pole> query = ofy().load().type(Pole.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Pole> queryIterator = query.iterator();
        List<Pole> poleList = new ArrayList<Pole>(limit);
        while (queryIterator.hasNext()) {
            poleList.add(queryIterator.next());
        }
        return CollectionResponse.<Pole>builder().setItems(poleList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(
            name = "getModified",
            path = "pole/sync/{userid}/{timestamp}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Pole> getModified(@Named("userid") String userid, @Named("timestamp") long timestamp, @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<Pole> query = ofy().load()
                .type(Pole.class)
                .filter(FilterHelper.getSyncFilter(userid, timestamp))
                .limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Pole> queryIterator = query.iterator();
        List<Pole> poleList = new ArrayList<Pole>(limit);
        while (queryIterator.hasNext()) {
            poleList.add(queryIterator.next());
        }
        return CollectionResponse.<Pole>builder().setItems(poleList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(String id) throws NotFoundException {
        try {
            ofy().load().type(Pole.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Pole with ID: " + id);
        }
    }
}