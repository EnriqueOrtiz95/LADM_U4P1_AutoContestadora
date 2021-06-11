package mx.edu.ladm_u4p1_autocontestadora_enriqueortiz

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context

class BaseDatos(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int ) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(bd: SQLiteDatabase) {
        bd.execSQL("CREATE TABLE ENTRANTES(CELULAR VARCHAR(200), MENSAJE VARCHAR(2000))")

    }

    override fun onUpgrade(bd: SQLiteDatabase?,p0: Int, p1: Int) {

    }

}