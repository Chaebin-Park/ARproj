package com.example.arproj.helper

import android.os.Environment
import android.util.Log
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.lang.StringBuilder

class CsvWriter(private val filePath: String) {
    fun write(fileName: String, dataList: ArrayList<Array<*>>){
        try{
            val fw = BufferedWriter(FileWriter("$filePath/$fileName", true))

            for(array in dataList){
                fw.write(processArray(array))
                fw.newLine()
            }

            fw.flush()
            fw.close()
        } catch (e: IOException){
            Log.e("ERROR", e.stackTraceToString())
        }
    }

    private fun processArray(data: Array<*>): String{
        val sb = StringBuilder()

        for(item in data){
            if(sb.length > 1){
                sb.append(',')
            }
            sb.append(item)
        }

        return sb.toString()
    }
}