package com.biga.menli.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.biga.menli.R;
import com.biga.menli.model.Usuario;
import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContatosAdapter extends RecyclerView.Adapter<ContatosAdapter.MyViewHolder> {

    private List<Usuario> contatos;
    private Context context;

    public ContatosAdapter(List<Usuario> listaContatos, Context c) {
        this.contatos = listaContatos;
        this.context = c;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemlista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contatos, parent, false);
        return new MyViewHolder(itemlista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Usuario usuario = contatos.get(position);

        holder.nome.setText( usuario.getNome() );

        if (usuario.getFoto() != null){

            Uri uri = Uri.parse( usuario.getFoto() );
            Glide.with( context ).load(uri).into( holder.foto );
        }else {
            holder.foto.setImageResource(R.drawable.padrao);
        }

    }

    @Override
    public int getItemCount() {
        return contatos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome;


        public MyViewHolder(View itemView){
            super(itemView);

            foto = itemView.findViewById(R.id.imageViewFotoContato);
            nome = itemView.findViewById(R.id.textNomeContato);

        }

    }


}
