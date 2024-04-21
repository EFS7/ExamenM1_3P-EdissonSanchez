package com.esdesignhn.examenm1_3p.Data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.esdesignhn.examenm1_3p.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.io.ByteArrayOutputStream;

public class ListAdapterEntrevista extends RecyclerView.Adapter<ListAdapterEntrevista.ViewHolder> {
    private List<Entrevista> data;
    private LayoutInflater inflador;
    private Context contexto;
    private ListAdapterEntrevista.OnItemDoubleClickListener doubleClickListener;
    public static int itemSelecionado = -1;
    ImageView foto;

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    public interface OnItemDoubleClickListener {
        void onItemDoubleClick(Entrevista entrevista);
    }
    public ListAdapterEntrevista(List<Entrevista> itemList, Context context, ListAdapterEntrevista.OnItemDoubleClickListener doubleClickListener){
        this.inflador = LayoutInflater.from(context);
        this.contexto = context;
        this.data = itemList;
        this.doubleClickListener = doubleClickListener;

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static int getItemSelecionado(){
        return itemSelecionado;
    }

    @Override
    public ListAdapterEntrevista.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflador.inflate(R.layout.entrevista_plantilla, null);
        return new ListAdapterEntrevista.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListAdapterEntrevista.ViewHolder holder, final int position) {
        holder.bindData(data.get(position));

        final int currentPosition = position;
        holder.itemView.setSelected(position == itemSelecionado);

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                itemSelecionado = currentPosition;
            }
        });

    }

    public void setItems(List<Entrevista> items){
        data = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView descripcion, periodista, fecha, idOrden;

        ViewHolder(View itemView) {
            super(itemView);
            descripcion = (TextView) itemView.findViewById(R.id.descripcion_txtV);
            periodista = (TextView) itemView.findViewById(R.id.periodista_txtV);
            fecha = (TextView) itemView.findViewById(R.id.fecha_txtV);
            idOrden = (TextView) itemView.findViewById(R.id.idOrden_txtV);
            foto = (ImageView) itemView.findViewById(R.id.foto_imgV);
        }

        void bindData(final Entrevista entrevista){
            descripcion.setText(entrevista.getDescripcion());
            periodista.setText(entrevista.getPeriodista());
            fecha.setText(entrevista.getFecha());
            idOrden.setText(entrevista.getId_firebase());
            Bitmap img = foto_decode(entrevista.getImagen());
            foto.setImageBitmap(img);
        }
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
}
