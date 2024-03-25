package com.tar.iq.activity.favourite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.tar.iq.R;
import com.tar.iq.activity.dashboard.MainActivity;
import com.tar.iq.adapter.FilmeRecyclerViewAdapter;
import com.tar.iq.api.DataService;
import com.tar.iq.model.Filme;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.tar.iq.util.Constantes.BASE_URL;

public class FavoritosActivity extends AppCompatActivity {

    private FilmeRecyclerViewAdapter filmeRecyclerViewAdapter;
    private RecyclerView recyclerView;
    private Retrofit retrofit;
    private List<Filme> listaDeFilmes;
    private TextView titulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFE4E1"));
        }
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);

        titulo = findViewById(R.id.textToolbar);
        titulo.setText("Favoritos");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        configurarObjetos();

        }

    @Override
    protected void onRestart() {
        super.onRestart();
        listaDeFilmes.clear();
        filmeRecyclerViewAdapter.notifyDataSetChanged();
        pesquisarRetrofit();
    }

    //--------------------------------------------------------------------------------------------
    private void configurarObjetos(){
        listaDeFilmes = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerAll);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        filmeRecyclerViewAdapter = new FilmeRecyclerViewAdapter(this, listaDeFilmes);
        recyclerView.setAdapter(filmeRecyclerViewAdapter);
        filmeRecyclerViewAdapter.notifyDataSetChanged();
        pesquisarRetrofit();
    }
    //---------------------------------------------------------------------------------------------
    private void pesquisarRetrofit() {
        Cursor cursor = MainActivity.banco.rawQuery("SELECT*FROM Filme",null);
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DataService servicosRetrofit = retrofit.create(DataService.class);
        if((cursor != null) && (cursor.getCount() > 0)){
            while (cursor.moveToNext())
            {
                String id = cursor.getString(cursor.getColumnIndex("imdbID"));
                Call<Filme> call = servicosRetrofit.recuperarFilmesId(id);

                call.enqueue(new Callback<Filme>() {
                    @Override
                    public void onResponse(Call<Filme> call, retrofit2.Response<Filme> response) {

                        if (response.isSuccessful()) {
                            Filme filme = response.body();
                            filme.setType("Type: " + filme.getType());
                            filme.setYear("Year: " + filme.getYear());
                            listaDeFilmes.add(filme);
                            filmeRecyclerViewAdapter.notifyDataSetChanged();
                        } else {
                        }
                    }

                    @Override
                    public void onFailure(Call<Filme> call, Throwable t) {
                    }

                });

            }
            cursor.close();

        }
        else{
            Toast.makeText(getApplicationContext(), "You do not have anything except the favorites list.", Toast.LENGTH_SHORT).show();

        }
    }
}
