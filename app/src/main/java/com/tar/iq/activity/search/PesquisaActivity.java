package com.tar.iq.activity.search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.tar.iq.R;
import com.tar.iq.activity.dashboard.MainActivity;
import com.tar.iq.adapter.FilmeRecyclerViewAdapter;
import com.tar.iq.api.DataService;
import com.tar.iq.model.Filme;
import com.tar.iq.model.ResultadoPesquisa;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.tar.iq.util.Constantes.BASE_URL;

public class PesquisaActivity extends AppCompatActivity {
    private ImageView imgPesquisar;
    private EditText edtPesquisa;


    private FilmeRecyclerViewAdapter filmeRecyclerViewAdapter;
    private RecyclerView recyclerView;
    private List<Filme> listaDeFilmes;
    private List<Filme> listaFavoritos;
    private Retrofit retrofit;
    private TextView titulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesquisa);
        carregarListaFavoritos();
        titulo = findViewById(R.id.textToolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFE4E1"));
        }
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        //toolbar.setTitle("Pesquisa");
        titulo.setText("Search");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imgPesquisar = findViewById(R.id.imgPesquisa);
        edtPesquisa = findViewById(R.id.txtPesquisa);

        configurarObjetos();
    }
    //--------------------------------------------------------------------------------------------------

    public void pesquisar(View v){
        if(!TextUtils.isEmpty(edtPesquisa.getText().toString())){
            pesquisarRetrofit(edtPesquisa.getText().toString());
        }
        else{
            Toast.makeText(getApplicationContext(), "Informe um título para pesquisar!", Toast.LENGTH_SHORT).show();
        }
    }
    //-----------------------------------------------------------------------------------------------------
    private void configurarObjetos(){
        listaDeFilmes = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerAll);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        filmeRecyclerViewAdapter = new FilmeRecyclerViewAdapter(this, listaDeFilmes);
        recyclerView.setAdapter(filmeRecyclerViewAdapter);
        filmeRecyclerViewAdapter.notifyDataSetChanged();
    }
    //----------------------------------------------------------------------------------------------------
    private void carregarListaFavoritos(){
        listaFavoritos = new ArrayList<Filme>();
        Cursor cursor = MainActivity.banco.rawQuery("SELECT*FROM Filme",null);
        while (cursor.moveToNext()){
            Filme f = new Filme();
            f.setImdbID(cursor.getString(cursor.getColumnIndex("imdbID")));
            listaFavoritos.add(f);
        }
    }
    //---------------------------------------------------------------------------------------------
    private void pesquisarRetrofit(String searchTerm) {

        listaDeFilmes.clear();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DataService servicosRetrofit = retrofit.create(DataService.class);

        Call<ResultadoPesquisa> call = servicosRetrofit.recuperarFilmesTitulos(searchTerm,"2000");

        call.enqueue(new Callback<ResultadoPesquisa>() {
            @Override
            public void onResponse(Call<ResultadoPesquisa> call, retrofit2.Response<ResultadoPesquisa> response) {

                if (response.isSuccessful()) {
                    Log.i("tariq", "vody: " + response.body());

                    ResultadoPesquisa resultadoPesquisa = response.body();

                    List<Filme>listaFilmes = resultadoPesquisa.getSearch();

                    if (listaFilmes != null) {
                        for (int i = 0; i < listaFilmes.size(); i++) {

                            Filme filme;
                            listaFilmes.get(i).setType("Tipo: "+listaFilmes.get(i).getType());
                            listaFilmes.get(i).setYear("Ano: "+listaFilmes.get(i).getYear());
                            filme = listaFilmes.get(i);
                            listaDeFilmes.add(filme);
                            filmeRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    } else
                        Toast.makeText(getApplicationContext(), "No results found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResultadoPesquisa> call, Throwable t) {
                Log.i("tariq", "Erro: " + t.getMessage());
            }

        });
    }
}
