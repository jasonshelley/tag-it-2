package com.jso.tagit.backend;

import com.google.appengine.api.datastore.Query;

/**
 * Created by Jason on 10/04/2016.
 */
public class FilterHelper {
    public static Query.Filter getSyncFilter(String userid, long last_modified)
    {
        Query.Filter useridFilter =
                new com.google.appengine.api.datastore.Query.FilterPredicate("userid",
                        com.google.appengine.api.datastore.Query.FilterOperator.EQUAL,
                        userid);

        Query.Filter lastModifiedFilter =
                new com.google.appengine.api.datastore.Query.FilterPredicate("last_modified",
                        com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL,
                        last_modified);

        Query.Filter syncFilter =
                com.google.appengine.api.datastore.Query.CompositeFilterOperator.and(useridFilter, lastModifiedFilter);

        return syncFilter;
    }

    public static Query.Filter getSyncFilter(long last_modified)
    {
        Query.Filter lastModifiedFilter =
                new com.google.appengine.api.datastore.Query.FilterPredicate("last_modified",
                        com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL,
                        last_modified);

        return lastModifiedFilter;
    }
}
