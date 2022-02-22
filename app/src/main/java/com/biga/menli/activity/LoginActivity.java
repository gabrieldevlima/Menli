package com.biga.menli.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.biga.menli.R;
import com.biga.menli.config.ConfiguracaoFirebase;
import com.biga.menli.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
    }

    public void logarUsuario(Usuario usuario){

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {


                if (task.isSuccessful()){
                    abrirTelaPrincipal();
                }else {

                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excecao = "Usuário não está cadastrado.";

                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "E-mail e senha não correspondem a usuário";
                    }catch (Exception e){
                        excecao = "Erro ao autenticar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this,excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void validarAutenticacaoUsuario(View view){

        //Recuperar textos dos campos
        String email = campoEmail.getText().toString();
        String senha = campoSenha.getText().toString();


        if(!email.isEmpty()){ // verifica o e-mail

            if(!senha.isEmpty()){ // verifica a senha

               Usuario usuario = new Usuario();
               usuario.setEmail( email );
               usuario.setSenha( senha );

               logarUsuario( usuario );

            }else {

                Toast.makeText(LoginActivity.this,"Preencha a senha!", Toast.LENGTH_SHORT).show();
            }

        }else{

            Toast.makeText(LoginActivity.this,"Preencha o e-mail!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual != null){
            abrirTelaPrincipal();
        }
    }

    public void abrirTelaCadastro(View view){
        Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
        startActivity( intent );

    }

    public void abrirTelaPrincipal(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity( intent );

    }
}
