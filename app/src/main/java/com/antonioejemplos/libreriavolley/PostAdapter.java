package com.antonioejemplos.libreriavolley;

/**
 * Created by Susana on 16/03/2016.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class PostAdapter extends ArrayAdapter {

    // Atributos
    private RequestQueue requestQueue;//Cola de peticiones de Volley. se encarga de gestionar automáticamente el envió de las peticiones, la administración de los hilos, la creación de la caché y la publicación de resultados en la UI.
    JsonObjectRequest jsArrayRequest;//Tipo de petición Volley utilizada...
    private String URL_BASE = "http://petty.hol.es/volley";//Url del host donde están alojados los archivos
    private static final String URL_JSON = "/social_media.json";//Nombre del archivo json alojado en el host...
    private static final String TAG = "PostAdapter";//Constante para gestionar la escritura en el Log
    List<Post> items;//Post es la clase que encapsula los datos de los elementos de la lista

    public PostAdapter(Context context) {
        super(context, 0);

        //1-Creación de la cola de peticiones.
        /*Existen cuatro tipos de peticiones estándar:
        1-StringRequest: Este es el tipo más común, ya que permite solicitar un recurso con formato de texto plano, como son los documentos HTML.
        2-ImageRequest: Como su nombre lo indica, permite obtener un recurso gráfico alojado en un servidor externo.
        3-JsonObjectRequest: Obtiene una respuesta de tipo JSONObject a partir de un recurso con este formato.
        4-JsonArrayRequest: Obtiene como respuesta un objeto del tipo JSONArray a partir de un formato JSON.*/
        //2- Gestionar petición del archivo JSON
        /*La primera petición que haremos será del tipo JsonObjectRequest, ya que tenemos un objeto JSON con un atributo llamado
        “ítems” de tipo array.
        El nombre del archivo es social_media.json.
        El primer parámetro del constructor es el método empleado en la petición. Como ves se usa la interfaz Method perteneciente
        a la clase Response, la cual contiene los métodos necesarios, como en este caso, donde se usa GET. Luego sigue la URL del
        recurso JSON, la cual se compone de dos strings concatenadas. El tercer parámetro son los pares clave-valor si se fuese a
        realizar una petición POST.
        El cuarto parámetro es una clase anónima del tipo Response.Listener para definir una escucha que maneje los resultados de
        la petición.
        Se debe sobrescribir el método onResponse() para codificar las acciones con la respuesta en el hilo de UI, en este caso
        parseamos el contenido del JSONObject y se le asigna el resultado al atributo ítems de nuestro adaptador.
        Adicionalmente se le indica al adaptador que actualice su contenido con notifyDataSetChanged(), ya que no sabremos en qué
        momento la petición terminará exitosamente.
        El quinto parámetro es una escucha que maneja los errores ocurridos en la transacción Http. Para ello se usa la clase
        Response.ErrorListener, la cual requiere dictaminar las acciones en su método onErrorResponse(). En este caso usamos el
        método estático d() de la clase Log para registrar en consola que ocurrió un error.
        Finalmente usamos el método add() para añadir la petición a la cola de peticiones:*/

        requestQueue= Volley.newRequestQueue(context);

        // Nueva petición JSONObject

        jsArrayRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL_BASE + URL_JSON,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        items = parseJson(response);
                        notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error Respuesta en JSON: " + error.getMessage());

                    }
                }
        );
        // Añadir petición a la cola
        requestQueue.add(jsArrayRequest);


    }

    @Override
    public int getCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        // Referencia del view procesado
        View listItemView;

        //Comprobando si el View no existe
        listItemView = null == convertView ? layoutInflater.inflate(
                R.layout.post,
                parent,
                false) : convertView;


        // Obtener el item actual
        Post item = items.get(position);

        // Obtener Views
        TextView textoTitulo = (TextView) listItemView.
                findViewById(R.id.textoTitulo);
        TextView textoDescripcion = (TextView) listItemView.
                findViewById(R.id.textoDescripcion);
        final ImageView imagenPost = (ImageView) listItemView.
                findViewById(R.id.imagenPost);

        // Actualizar los Views
        textoTitulo.setText(item.getTitulo());
        textoDescripcion.setText(item.getDescripcion());

        // Petición para obtener la imagen
        /*En este situación usaremos el tipo ImageRequest para obtener cada imagen. Solo necesitamos concatenar
        la url absoluta que fue declarada como atributo, más la dirección relativa que cada imagen trae consigo
        en el objeto JSON*/
        ImageRequest request = new ImageRequest(
                URL_BASE + item.getImagen(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        imagenPost.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        imagenPost.setImageResource(R.drawable.error);
                        Log.d(TAG, "Error en respuesta Bitmap: "+ error.getMessage());
                    }
                });

        // Añadir petición a la cola
        requestQueue.add(request);


        return listItemView;


    }
    public List<Post> parseJson(JSONObject jsonObject){
        // Variables locales
        List<Post> posts = new ArrayList<>();
        JSONArray jsonArray= null;

        try {
            // Obtener el array del objeto
            jsonArray = jsonObject.getJSONArray("items");

            for(int i=0; i<jsonArray.length(); i++){

                try {
                    JSONObject objeto= jsonArray.getJSONObject(i);

                    Post post = new Post(
                            objeto.getString("titulo"),
                            objeto.getString("descripcion"),
                            objeto.getString("imagen"));


                    posts.add(post);

                } catch (JSONException e) {
                    Log.e(TAG, "Error de parsing: "+ e.getMessage());
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return posts;
    }

}