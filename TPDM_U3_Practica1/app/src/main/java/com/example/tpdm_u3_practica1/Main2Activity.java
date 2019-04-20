package com.example.tpdm_u3_practica1;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {
    EditText nombre, matricula, carrera,semestre;
    Button insertar, eliminar,consultar,actualizar;
    private DatabaseReference mDatabase;
    List<estudiante> datos;
    ListView listado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        nombre = findViewById(R.id.nombreEst);
        matricula = findViewById(R.id.matriculaEst);
        carrera = findViewById(R.id.carreraEst);
        semestre = findViewById(R.id.semestreEst);

        insertar = findViewById(R.id.insertarEst);
        eliminar = findViewById(R.id.eliminarEst);
        //actualizar = findViewById(R.id.actualizarEst);
        consultar = findViewById(R.id.consultarEst);

        listado = findViewById(R.id.listaEst);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //llena la lista con datos
        mDatabase.child("Estudiante").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                datos = new ArrayList<>();

                if(dataSnapshot.getChildrenCount()<=0){
                    Toast.makeText(Main2Activity.this, "ERROR: No hay datos", Toast.LENGTH_SHORT).show();
                    return;
                }

                for(final DataSnapshot snap : dataSnapshot.getChildren()){
                    mDatabase.child("Estudiante").child(snap.getKey()).addValueEventListener(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    estudiante est = dataSnapshot.getValue(estudiante.class);

                                    if(est!=null){
                                        datos.add(est);
                                    }
                                    cargarSelect();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            }
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





        insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final estudiante est = new estudiante(carrera.getText().toString(), nombre.getText().toString(), matricula.getText().toString(), semestre.getText().toString());

                mDatabase.child("Estudiante").child(est.matricula).setValue(est)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Main2Activity.this, "EXITO", Toast.LENGTH_SHORT).show();
                                nombre.setText("");carrera.setText("");semestre.setText("");matricula.setText("");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Main2Activity.this, "ERROR!!!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solicitarEliminar();
            }
        });

        consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solicitarConsulta();
            }
        });



    }

    private void solicitarConsulta() {
        final EditText id = new EditText(this);
        id.setHint("ID A BUSCAR");
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("ATENCION").setMessage("VALOR A BUSCAR:").setView(id).setPositiveButton("Buscar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mostrar(id.getText().toString());
            }
        }).setNegativeButton("Cancelar", null).show();
    }
    private void mostrar(String i){
        FirebaseDatabase.getInstance().getReference().child("Estudiante").child(i)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        estudiante est = dataSnapshot.getValue(estudiante.class);

                        if(est!=null) {
                            nombre.setText(est.nombre);
                            semestre.setText(est.semestr);
                            matricula.setText(est.matricula);
                            carrera.setText(est.carrera);
                        } else {
                            mensaje("Error","No se encontró dato a mostrar");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private void solicitarEliminar() {
        final EditText id = new EditText(this);
        id.setHint("MATRICULA A ELIMINAR");
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("ATENCION").setMessage("VALOR A BUSCAR:").setView(id).setPositiveButton("ELIMINAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eliminar(id.getText().toString());
            }
        }).setNegativeButton("Cancelar", null).show();

    }
    private void eliminar(String id){
        mDatabase.child("Estudiante").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Main2Activity.this, "SE ELIMINO CORRECTAMENTE", Toast.LENGTH_SHORT).show();
                        matricula.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Main2Activity.this, "ERROR AL ELIMINAR", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    ///otras funciones
    private void cargarSelect(){
        if (datos.size()==0) return;
        String nombres[] = new String[datos.size()];

        for(int i = 0; i<nombres.length; i++){
            estudiante est = datos.get(i);
            nombres[i] = est.nombre;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombres);
        listado.setAdapter(adapter);
    }

    private void mensaje(String t, String m){
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);

        alerta.setTitle(t).setMessage(m).setPositiveButton("OK",null).show();
    }
}


