package com.example.arproj.helper

import android.util.Log
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class CsvHelper(private val filePath: String) {

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

    companion object{
        val TAG: String = this::class.java.simpleName
    }
}