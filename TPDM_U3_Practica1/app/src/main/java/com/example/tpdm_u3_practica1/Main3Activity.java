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

public class Main3Activity extends AppCompatActivity {
    EditText nombre, area, titulo,id;
    Button insertar, eliminar,consultar,actualizar;
    private DatabaseReference mDatabase;
    List<Maestro> datos;
    ListView listado;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        nombre = findViewById(R.id.nombreMa);
        area = findViewById(R.id.areaMa);
        titulo = findViewById(R.id.tituloMa);
        id = findViewById(R.id.idMa);

        insertar = findViewById(R.id.insertarMa);
        eliminar = findViewById(R.id.eliminarMa);
        //actualizar = findViewById(R.id.actualizarEst);
        consultar = findViewById(R.id.consultarMa);

        listado = findViewById(R.id.listaMa);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        //llena la lista con datos
        mDatabase.child("maestro").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                datos = new ArrayList<>();

                if(dataSnapshot.getChildrenCount()<=0){
                    Toast.makeText(Main3Activity.this, "ERROR: No hay datos", Toast.LENGTH_SHORT).show();
                    return;
                }

                for(final DataSnapshot snap : dataSnapshot.getChildren()){
                    mDatabase.child("maestro").child(snap.getKey()).addValueEventListener(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Maestro ma = dataSnapshot.getValue(Maestro.class);

                                    if(ma!=null){
                                        datos.add(ma);
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
                final Maestro ma = new Maestro(area.getText().toString(),  id.getText().toString(),  nombre.getText().toString(),  titulo.getText().toString());

                mDatabase.child("maestro").child(ma.id).setValue(ma)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Main3Activity.this, "EXITO", Toast.LENGTH_SHORT).show();
                                nombre.setText("");id.setText("");area.setText("");titulo.setText("");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Main3Activity.this, "ERROR!!!", Toast.LENGTH_SHORT).show();
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
        FirebaseDatabase.getInstance().getReference().child("maestro").child(i)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Maestro ma = dataSnapshot.getValue(Maestro.class);

                        if(ma!=null) {
                            nombre.setText(ma.nombre);
                            id.setText(ma.id);
                            titulo.setText(ma.titulo);
                            area.setText(ma.area);
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
        mDatabase.child("maestro").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Main3Activity.this, "SE ELIMINO CORRECTAMENTE", Toast.LENGTH_SHORT).show();
                        Main3Activity.this.id.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Main3Activity.this, "ERROR AL ELIMINAR", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    ///otras funciones
    private void cargarSelect(){
        if (datos.size()==0) return;
        String nombres[] = new String[datos.size()];

        for(int i = 0; i<nombres.length; i++){
            Maestro ma = datos.get(i);
            nombres[i] = ma.nombre;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombres);
        listado.setAdapter(adapter);
    }

    private void mensaje(String t, String m){
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);

        alerta.setTitle(t).setMessage(m).setPositiveButton("OK",null).show();
    }
}
