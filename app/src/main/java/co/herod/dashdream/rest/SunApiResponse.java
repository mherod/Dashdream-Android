package co.herod.dashdream.rest;

import com.google.gson.annotations.Expose;

import org.parceler.Parcel;

// generated with http://www.jsonschema2pojo.org

@Parcel
public class SunApiResponse {

    @Expose
    private Results results;
    @Expose
    private String status;

    /**
     * @return The results
     */
    public Results getResults() {
        return results;
    }

    /**
     * @param results The results
     */
    public void setResults(Results results) {
        this.results = results;
    }

    /**
     * @return The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

}
