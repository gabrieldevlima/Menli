package com.biga.menli.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.biga.menli.R;
import com.biga.menli.config.ConfiguracaoFirebase;
import com.biga.menli.helper.Permissao;
import com.biga.menli.helper.UsuarioFirebase;
import com.biga.menli.model.Usuario;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private ImageButton imageButtonCamera, imageButtonGaleria;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private CircleImageView circleImageViewPerfil;
    private ImageView imageAtualizarNome;
    private StorageReference storageReference;
    private String identificadorUsuario;
    private EditText editPerfilNome;
    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        //Configurações Iniciais

        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //validar permissoes
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        imageButtonCamera = findViewById(R.id.imageButtonCamera);
        imageButtonGaleria = findViewById(R.id.imageButtonGaleria);
        circleImageViewPerfil = findViewById(R.id.circleImageViewFotoPerfil);
        editPerfilNome = findViewById(R.id.editPerfilNome);
        imageAtualizarNome =findViewById(R.id.imageAtualizarNome);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Ajustes");
        setSupportActionBar( toolbar );

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recuperar Dados Usuário
        final FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();

        if( url != null){
            Glide.with(ConfiguracoesActivity.this).load(url).into(circleImageViewPerfil);
        }else {
            circleImageViewPerfil.setImageResource(R.drawable.padrao);
        }

        editPerfilNome.setText( usuario.getDisplayName());

        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, SELECAO_CAMERA);
                }

            }
        });

        imageButtonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, SELECAO_GALERIA);
                }

            }
        });

        imageAtualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nome = editPerfilNome.getText().toString();
                boolean retorno = UsuarioFirebase.atualizarNomeUsuario(nome);
                if ( retorno ) {

                    usuarioLogado.setNome( nome );
                    usuarioLogado.atualizar();

                    Toast.makeText(ConfiguracoesActivity.this,
                            "Nome alterado com sucesso!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {

                switch ( requestCode ){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;

                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                if (imagem != null){

                    circleImageViewPerfil.setImageBitmap( imagem );

                    //Recupera imagem no Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    imagem.compress(Bitmap.CompressFormat.JPEG,70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no Firebase
                    final StorageReference imagemRef = storageReference
                            .child("Imagens")
                            .child("Perfil")
                            .child(identificadorUsuario + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfiguracoesActivity.this,
                                    "Erro ao fazer upload da imagem",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri url) {
                                    atualizarFotoUsuario( url );
                                    Toast.makeText(ConfiguracoesActivity.this,
                                            "Sucesso ao fazer upload da imagem",Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void atualizarFotoUsuario(Uri url){
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);
        if ( retorno ){
            usuarioLogado.setFoto( url.toString());
            usuarioLogado.atualizar();

            Toast.makeText(ConfiguracoesActivity.this,
                    "Sua foto foi alterada!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for ( int permissaoResultado: grantResults ){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utulizar o app é nessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
