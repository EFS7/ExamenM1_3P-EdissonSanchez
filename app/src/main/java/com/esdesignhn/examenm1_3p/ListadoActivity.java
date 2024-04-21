package com.esdesignhn.examenm1_3p;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.esdesignhn.examenm1_3p.Data.Entrevista;
import com.esdesignhn.examenm1_3p.Data.ListAdapterEntrevista;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListadoActivity extends AppCompatActivity {
    private FirebaseFirestore firebase;
    String coleccion;
    ListAdapterEntrevista listAdapter;
    private List<Entrevista> listEntrevistas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);

        firebase = FirebaseFirestore.getInstance();
        coleccion = "entrevista";
        obtenerLista();

    }

    private  void obtenerLista() {
        obtenerEntrevistasFirebase();

        listAdapter = new ListAdapterEntrevista(listEntrevistas, this, new ListAdapterEntrevista.OnItemDoubleClickListener() {
           @Override
           public void onItemDoubleClick(Entrevista entrevista){
                Log.d("Test","Test");
           }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViewEntrevistas);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listAdapter);


    }

    private void obtenerEntrevistasFirebase(){
        listEntrevistas = new ArrayList<>();
        firebase.collection(coleccion)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (DocumentSnapshot document : task.getResult()){
                                Integer idOrden = 0;
                                String id_fb = document.getId();
                                String descripcion = document.getString("descripcion");
                                String periodista = document.getString("periodista");
                                String fecha = document.getString("fecha");
                                String imagen = document.getString("imagen");
                                String audio = document.getString("audio");
                                listEntrevistas.add(new Entrevista(idOrden,id_fb,descripcion,periodista,fecha,imagen,audio));
                            }
                            listAdapter.notifyDataSetChanged();
                        }else{
                            Log.d(TAG, "Error al obtener los datos. ", task.getException());
                        }
                    }
                });
    }

}