package mx.edu.ladm_u4p1_autocontestadora_enriqueortiz

import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.net.Uri
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {
    val siPermiso = 1
    val siPermisoRecived = 2
    val siPermisoLectura = 3
    val siLecturaContactos = 18

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.RECEIVE_SMS )
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.RECEIVE_SMS),siPermisoRecived)
        }
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_SMS),siPermisoLectura)
        }else
        {
            leerSMSEntrada()
        }



        textView2.setOnClickListener{
            try{
                val cursor = BaseDatos(this, "entrantes", null, 1).writableDatabase
                    .rawQuery("SELECT * FROM ENTRANTES", null)
                var ultimo = ""
                if (cursor.moveToFirst()){
                    do{
                        ultimo = "ULTIMO MENSAJE RECIBIDO\nCELULAR ORIGEN: " + cursor.getString(0) + "\nMENSAJE SMS: " + cursor.getString(1)
                    }while(cursor.moveToNext())
                } else {
                    ultimo = "SIN MENSAJES AUN, TABLA VACIA"
                }
                textView2.setText(ultimo)

            } catch (err: SQLiteException) {
                Toast.makeText(this, err.message, Toast.LENGTH_LONG).show()
            }
        }

        btnSMS.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.SEND_SMS),siPermiso)
            }else {
                envioSMS()
            }
        }


        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_CONTACTS), siLecturaContactos)
        }
        btnLeerContacto.setOnClickListener {
            cargarListaContactos()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == siPermiso){
            envioSMS()
        }
        if (requestCode == siPermisoRecived){
            mensajeRecibir()
        }
        if (requestCode == siPermisoLectura){
            leerSMSEntrada()
        }
        if(requestCode == siLecturaContactos){
            setTitle("PERMISO OTORGADO")
        }
    }

    private fun mensajeRecibir() {
        AlertDialog.Builder(this)
                .setMessage("MENSAJE RECIBIDO")
                .show()
    }

    private fun leerSMSEntrada() {
        var cursor = contentResolver.query(Uri.parse("content://sms/"),null,null,null,null)
        var resultado = ""
        if (cursor!!.moveToFirst()){
            var posColcelularOrigen = cursor.getColumnIndex("address")
            var posColMensaje = cursor.getColumnIndex("body")
            val posColFecha = cursor.getColumnIndex("date")
            do{
                val fechamensaje = cursor.getString(posColFecha)
                resultado += "ORIGEN: " +cursor.getString(posColcelularOrigen)+
                        "\nMENSAJE: "+cursor.getString(posColMensaje)+
                        "\nFECHA: "+ Date(fechamensaje.toLong()) +
                        "\n---------------\n"

            }while(cursor.moveToNext())
        }else{
            resultado = "NO HAY SMS EN BANDEJA DE ENTRADA"
        }
        textViewSMS.setText(resultado)
    }

    private fun envioSMS() {
        SmsManager.getDefault().sendTextMessage(editText.text.toString(),null,editText2.text.toString(),null,null)
        Toast.makeText(this,"Se envio el SMS",Toast.LENGTH_LONG)
                .show()
    }

    private fun cargarListaContactos() {
        var resultado = ""

        val cursorContactos = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,null,null,null
        )
        if (cursorContactos!!.moveToFirst()) {
            do {

                var idContacto = cursorContactos.getString(cursorContactos.getColumnIndex(
                        ContactsContract.Contacts._ID))
                var nombreContacto = cursorContactos.getString(cursorContactos.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME))
                var telefonosContactos  = ""
                if (cursorContactos.getInt(cursorContactos.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0){
                    var cursorCel = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf<String>(idContacto.toString()),null
                    )
                    while (cursorCel!!.moveToNext()){
                        telefonosContactos += cursorCel!!.getString(cursorCel.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER))+
                                "\n"
                    }
                    cursorCel.close()
                }
                resultado += "ID: "+idContacto+"\nNombre: "+nombreContacto+"\nTelefonos:\n"+
                        telefonosContactos+"\n------------\n"
            }while (cursorContactos.moveToNext())
            textView.setText(resultado)
        }else{
            resultado = "CONTACTOS:\nNO HAY CONTACTOS CAPTURADOS"
        }
    }
}