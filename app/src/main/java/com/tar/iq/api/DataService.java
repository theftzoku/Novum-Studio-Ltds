package com.tar.iq.api;

import com.tar.iq.model.Filme;
import com.tar.iq.model.ResultadoPesquisa;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import static com.tar.iq.util.Constantes.KEY;
import static com.tar.iq.util.Constantes.PARAMETRO_01_API_KEY;

public interface DataService {

    @Headers({
            "Content-Type: application/json;charset=utf-8",
            "Accept: application/json"
    })
    @GET(PARAMETRO_01_API_KEY+KEY)
    Call<ResultadoPesquisa> recuperarFilmesTitulo( @Query("s") String searchTerm, @Query("y") String year);

    @GET(PARAMETRO_01_API_KEY+KEY)
    Call<ResultadoPesquisa> recuperarFilmesTitulos(@Query("s") String title, @Query("y") String year);
    @GET(PARAMETRO_01_API_KEY+KEY)
    Call<Filme> recuperarFilmesId(@Query("i") String id);

}
