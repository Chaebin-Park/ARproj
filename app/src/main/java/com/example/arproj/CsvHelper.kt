package com.example.arproj

import android.util.Log
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class CsvHelper(private val filePath: String) {
    private val TAG = this.javaClass.simpleName

    fun writeData(fileName: String, dataList: ArrayList<Array<String>>){
        try{
            FileWriter(File("$filePath/$fileName")).use { fw->
                CSVWriter(fw).use {
                    it.writeAll(dataList)
                }
            }
        } catch (e: IOException){
            Log.e(TAG, e.stackTraceToString())
        }
    }
}