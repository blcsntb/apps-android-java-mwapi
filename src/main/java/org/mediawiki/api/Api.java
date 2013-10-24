package org.mediawiki.api;

import com.github.kevinsawicki.http.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Client wrapper for connecting to a MediaWiki installation's API.
 *
 * - It aims to be fully reentrant.
 * - Uses JSON for everything
 */
public class Api {

    /**
     * Parameter to {@link #makeRequest(int, RequestBuilder)}, performs GET request.
     */
    public static final int METHOD_GET = 1;
    /**
     * Parameter to {@link #makeRequest(int, RequestBuilder)}, performs POST request.
     */
    public static final int METHOD_POST = 2;

    /**
     * The exact URL to which API requests are made.
     */
    private URL apiUrl;

    /**
     * Create an Api class with the default endpoint path.
     *
     * Uses https by default (isSecure is true).
     * Default endpoint path is /w/api.php.
     *
     * @param domain Domain name of the server with the wiki to connect to
     */
    public Api(final String domain) {
        this(domain, true);
    }

    /**
     * Create an Api class with the default endpoint path.
     *
     * @param domain Domain name of the mediawiki installation to connect to
     * @param useSecure true to use https, false to use http
     */
    public Api(final String domain, final boolean useSecure) {
        this(domain, useSecure, "/w/api.php");
    }

    /**
     * Create an Api class with given hostname and endpoint path.
     *
     * @param domain Domain name of the mediawiki installation to connect to
     * @param useSecure true to use https, false to use http
     * @param endpointPath Path to the api.php file. Require preceding slash.
     */
    public Api(final String domain, final boolean useSecure, final String endpointPath) {
        String protocol;
        if (useSecure) {
            protocol = "https";
        } else {
            protocol = "http";
        }

        try {
            apiUrl = new URL(protocol, domain, endpointPath);
        } catch (MalformedURLException e) {
            // This never actually is supposed to happen, since it is thrown only
            // when an unknown protocal is given. http and https are guaranteed to be present,
            // according to http://docs.oracle.com/javase/6/docs/api/java/net/URL.html#URL(java.lang.String, java.lang.String, int, java.lang.String)
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the full Url to which API requests are directed.
     *
     * @return Full URL to which requests are directed
     */
    public URL getApiUrl() {
        return apiUrl;
    }

    /**
     * Start building a request for a particular action.
     *
     * @param action The <a href="https://www.mediawiki.org/wiki/API:Main_page#The_action">action</a> to start building
     *               a request for.
     * @return A {@link RequestBuilder} instance that can be used to add parameters & execute the request.
     */
    public RequestBuilder action(final String action) {
        return new RequestBuilder(this, action);
    }

    /**
     * Performs the HTTP request from the given requestBuilder, with the given HTTP method.
     *
     * Supports GET, POST and HEAD only currently, since that is all that the MW API supports.
     *
     * TODO: Figure out how to do error handling (network!)
     *
     * @param method HTTP method to use when performing the request
     * @param requestBuilder The requestBuilder to use to construct the request
     * @return A JSONObject with the response from the server
     */
    public JSONObject makeRequest(final int method, final RequestBuilder requestBuilder) {
        HttpRequest request;
        switch(method) {
            case METHOD_GET:
                request = HttpRequest.get(getApiUrl().toString(), requestBuilder.getParams(), true);
                break;
            case METHOD_POST:
                request = HttpRequest.post(getApiUrl().toString(), requestBuilder.getParams(), true);
                break;
            default:
                throw new IllegalArgumentException("Unkown argument passed for parameter method");
        }
        try {
            return new JSONObject(request.body());
        } catch (JSONException e) {
            // Well, if the request *does* complete successfully but the
            throw new RuntimeException(e);
        }
    }
}
