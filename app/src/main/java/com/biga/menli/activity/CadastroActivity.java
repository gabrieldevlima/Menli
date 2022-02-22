package com.biga.menli.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.biga.menli.R;
import com.biga.menli.config.ConfiguracaoFirebase;
import com.biga.menli.helper.Base64Custom;
import com.biga.menli.helper.UsuarioFirebase;
import com.biga.menli.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText campoNome, campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editPerfilNome);
        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
    }

    public void cadatrarUsuario(final Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
             usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if( task.isSuccessful() ){
                    Toast.makeText(CadastroActivity.this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                    finish();

                    try {

                        String identificadorUsuario = Base64Custom.codificarbase64( usuario.getEmail() );
                        usuario.setId( identificadorUsuario );
                        usuario.salva();

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else {

                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch ( FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    }catch ( FirebaseAuthInvalidCredentialsException e){
                        excecao = "Por favor, digite um e-mail válido";
                    }catch ( FirebaseAuthUserCollisionException e){
                        excecao = "Este usuário já foi cadastrado";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this,excecao, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void validarcadastroUsuario(View view){
        //Recuperar textos dos campos
        String textNome = campoNome.getText().toString();
        String textEmail = campoEmail.getText().toString();
        String textSenha = campoSenha.getText().toString();

        if(!textNome.isEmpty()){ // verifica o nome

            if(!textEmail.isEmpty()){ // verifica o e-mail

                if(!textSenha.isEmpty()){ // verifica a senha

                    Usuario usuario = new Usuario();
                    usuario.setNome( textNome );
                    usuario.setEmail( textEmail );
                    usuario.setSenha( textSenha );

                    cadatrarUsuario( usuario );

                }else {

                    Toast.makeText(CadastroActivity.this,"Preencha a senha!", Toast.LENGTH_SHORT).show();
                }

            }else {

                Toast.makeText(CadastroActivity.this,"Preencha o E-mail!", Toast.LENGTH_SHORT).show();
            }

        }else{

            Toast.makeText(CadastroActivity.this,"Preencha o nome!", Toast.LENGTH_SHORT).show();
        }
    }
}
