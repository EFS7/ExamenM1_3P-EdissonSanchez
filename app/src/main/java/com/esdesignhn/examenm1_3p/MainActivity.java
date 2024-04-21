package com.esdesignhn.examenm1_3p;

import static android.service.controls.ControlsProviderService.TAG;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.esdesignhn.examenm1_3p.Data.Entrevista;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore firebase;
    private String coleccion, fotoCodificada, audioCodificado;
    private EditText descripcion_txt, periodista_txt, fecha_txt;
    private Button guardar, listado;
    private ImageButton btn_camara, btn_audioRec, btn_audioPlay;
    private int ultID;
    private static final int REQUEST_PERMISSIONS = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView visor_img;
    private int img_default = R.drawable.ic_launcher_foreground;
    private MediaRecorder grabacion;
    private String audio_grabado;
    private boolean grabacionLive = false, audioLive = false;
    private String imgB64="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebase = FirebaseFirestore.getInstance();
        coleccion = "entrevista";

        obtenerID();


        descripcion_txt = (EditText) findViewById(R.id.descripcion_text);
        periodista_txt = (EditText) findViewById(R.id.periodista_text);
        fecha_txt = (EditText) findViewById(R.id.fecha_text);
        visor_img = (ImageView) findViewById(R.id.imagen_img);

        guardar = (Button) findViewById(R.id.guardar_btn);
        listado = (Button) findViewById(R.id.listado_btn);

        btn_camara = (ImageButton) findViewById(R.id.camara_btn);
        btn_audioRec = (ImageButton) findViewById(R.id.audioRec_btn);
        btn_audioPlay = (ImageButton) findViewById(R.id.audioPlay_btn);

        btn_audioPlay.setEnabled(false);

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CargarDatos();
            }
        });

        listado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent listado = new Intent(MainActivity.this, ListadoActivity.class);
                startActivity(listado);
            }
        });

        btn_camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
                alerta.setTitle("Origen de Imagen");
                alerta.setMessage("Desea Tomar una Foto nueva o Seleccionarla de su Album");
                alerta.setPositiveButton("Tomar Foto", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nuevaFoto();
                    }
                });
                alerta.setNegativeButton("Album", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("Album","Negativo");
                    }
                });
                alerta.show();
            }
        });

        btn_audioRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grabar_audio();
            }
        });

        btn_audioPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audio();
            }
        });

    }
    private Bitmap foto_decode(String img_base64){
        try{
            InputStream stream = new ByteArrayInputStream(img_base64.getBytes());
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            return bitmap;
        }catch (Exception e){
            return null;
        }

    }

    private void nuevaFoto() {
        String[] permisos = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        boolean permisosAutorizados = true;
        for (String permiso : permisos){
            if(ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED){
                permisosAutorizados = false;
                break;
            }
        }
        if (permisosAutorizados){
            abrirCamara();
        }else{
            ActivityCompat.requestPermissions(this, permisos, REQUEST_PERMISSIONS);
        }
    }

    private void abrirCamara() {
        Intent tomarFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(tomarFoto.resolveActivity(getPackageManager()) != null){
            startActivityForResult(tomarFoto, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(getApplicationContext(), "Error al abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int codigoRepuesta, int codResultado, @Nullable Intent dataFoto){
        super.onActivityResult(codigoRepuesta, codResultado, dataFoto);

        if(codigoRepuesta == REQUEST_IMAGE_CAPTURE && codResultado == RESULT_OK){
            Bundle foto = dataFoto.getExtras();
            if (foto != null) {
                Bitmap fotoBitmap = (Bitmap) foto.get("data");
                visor_img.setImageBitmap(fotoBitmap);
                fotoCodificada = fotoBase64(fotoBitmap);

                Toast.makeText(getApplicationContext(), "Foto Guardada", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "Error de Foto", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Foto Cancelada", Toast.LENGTH_SHORT).show();
        }

    }

    private void CargarDatos() {

        obtenerID();

        Integer IdOrden = ultID+1;
        String id_fb = "";
        String descripcion = descripcion_txt.getText().toString();
        String periodista = periodista_txt.getText().toString();
        String fecha = fecha_txt.getText().toString();
        String imagen = fotoCodificada;
        String audio = audioBase64(audio_grabado);

        Entrevista entrevista = new Entrevista(IdOrden,id_fb, descripcion, periodista, fecha, imagen, audio);

        firebase.collection(coleccion)
                .add(entrevista)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Entrevista Registrada ("+documentReference.getId()+")", Toast.LENGTH_SHORT).show();
                        limpiarForm();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error! ("+e+")", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void obtenerID() {

        firebase.collection(coleccion)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ultID = queryDocumentSnapshots.size();
                })
                .addOnFailureListener(error ->{
                    Log.e("Error", "Error al obtener datos: ("+error.getMessage());
                });

    }

    private void limpiarForm(){
        descripcion_txt.setText("");
        periodista_txt.setText("");
        fecha_txt.setText("");
        fotoCodificada = "";
        visor_img.setImageResource(img_default);

    }

    private String fotoBase64 (Bitmap foto){
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        foto.compress(Bitmap.CompressFormat.JPEG, 100, temp);
        byte[] imageBytes = temp.toByteArray();

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void grabar_audio (){
        String[] permisosA = {Manifest.permission.RECORD_AUDIO};
        if(grabacionLive == false){
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                grabacion = new MediaRecorder();
                audio_grabado = getExternalCacheDir().getAbsolutePath() + "/audio" + (ultID + 1) + ".3gp";

                grabacion.setAudioSource(MediaRecorder.AudioSource.MIC);
                grabacion.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                grabacion.setOutputFile(audio_grabado);
                grabacion.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                try {
                    grabacion.prepare();
                    grabacion.start();
                    grabacionLive = true;
                    btn_audioRec.setMaxHeight(5);
                    btn_audioRec.setImageResource(R.drawable.recicon_stop);
                    Toast.makeText(getApplicationContext(), "Grabacion en curso...", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                ActivityCompat.requestPermissions(this, permisosA, REQUEST_PERMISSIONS);
            }
        }else{
            grabacion.stop();
            grabacion.release();
            grabacion = null;
            btn_audioRec.setImageResource(R.drawable.recicon_start);
            grabacionLive = false;
            Toast.makeText(getApplicationContext(), "Grabacion finalizada", Toast.LENGTH_SHORT).show();
            btn_audioPlay.setEnabled(true);
        }

    }

    private void audio(){
        MediaPlayer audioP = new MediaPlayer();
        try {
            audioP.setDataSource(audio_grabado);
            audioP.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioP.start();
        Toast.makeText(getApplicationContext(), "Reproduciendo...", Toast.LENGTH_SHORT).show();
    }

    private String audioBase64 (String audio){
        try {
            File audioFile = new File(audio);
            long audioSize = audioFile.length();

            ByteArrayOutputStream temp = new ByteArrayOutputStream();
            FileInputStream file = new FileInputStream(audioFile);
            byte[] buffer =  new byte[1024];
            int bytesRead;
            while ((bytesRead = file.read(buffer)) != -1) {
                temp.write(buffer, 0, bytesRead);
            }
            byte[] audioBytes = temp.toByteArray();

            return Base64.encodeToString(audioBytes, Base64.DEFAULT);

        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}