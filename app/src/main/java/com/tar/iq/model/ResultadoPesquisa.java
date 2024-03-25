package com.tar.iq.model;

import java.util.List;

public class ResultadoPesquisa {

    private List<Filme> Search;
    private String totalResults;
    private String Response;

    public ResultadoPesquisa()
    {
    }


    public List<Filme> getSearch() {
        return Search;
    }

    public void setSearch(List<Filme> search) {
        Search = search;
    }

    public String getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(String totalResults) {
        this.totalResults = totalResults;
    }

    public String getResponse() {
        return Response;
    }

    public void setResponse(String response) {
        Response = response;
    }
}
