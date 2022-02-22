package com.biga.menli.activity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.biga.menli.config.ConfiguracaoFirebase;
import com.biga.menli.helper.Base64Custom;
import com.biga.menli.helper.UsuarioFirebase;
import com.biga.menli.model.Mensagem;
import com.biga.menli.model.Usuario;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.biga.menli.R;
import com.google.firebase.database.DatabaseReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private EditText editMensagem;
    private Usuario usuarioDestinatario;

    //identifacador usuarios remetente e destinatario
    private String idUsuarioRemente;
    private String idUsuarioDestinatario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurações iniciais
        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);

        //recupera dados do usuario destinatario
        idUsuarioRemente = UsuarioFirebase.getIdentificadorUsuario();


        //Recuperar dados do usuário destinatario
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
            textViewNome.setText( usuarioDestinatario.getNome() );

            String foto = usuarioDestinatario.getFoto();
            if (foto != null){
                Uri url = Uri.parse(usuarioDestinatario.getFoto());
                Glide.with(ChatActivity.this)
                        .load(url)
                        .into(circleImageViewFoto);
            }else{
                circleImageViewFoto.setImageResource(R.drawable.padrao);
            }

            //recuperar dados usuario destinatario
            idUsuarioDestinatario = Base64Custom.codificarbase64( usuarioDestinatario.getEmail());
        }

    }

    public void enviarMensagem(View view){

        String textoMensagem = editMensagem.getText().toString();

        if( !textoMensagem.isEmpty()){

            Mensagem mensagem = new Mensagem();
            mensagem.setIdUsuario( idUsuarioRemente );
            mensagem.setMensagem( textoMensagem );

            //salvar mensagens para o remetente
            salvarMensagem(idUsuarioRemente,idUsuarioDestinatario,mensagem);


        }else {
            Toast.makeText(ChatActivity.this,
                    "Digite uma mensagem para enviar!", Toast.LENGTH_LONG).show();
        }
    }

    private void salvarMensagem(String idRemente, String idDestinatario, Mensagem msg){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        //limpar texto
        editMensagem.setText("");

    }

}
