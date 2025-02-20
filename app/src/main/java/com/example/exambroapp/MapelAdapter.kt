package com.example.exambroapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class MapelAdapter(
    private val context: Context,
    private val mapelList: List<Mapel>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<MapelAdapter.MapelViewHolder>() {

    class MapelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val classTextView: TextView = itemView.findViewById(R.id.classTextView) // Tambahkan binding untuk kelas
        val linkTextView: TextView = itemView.findViewById(R.id.linkTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mapel, parent, false)
        return MapelViewHolder(view)
    }

    override fun onBindViewHolder(holder: MapelViewHolder, position: Int) {
        val mapel = mapelList[position]
        holder.nameTextView.text = mapel.name
        holder.classTextView.text = "Kelas: ${mapel.kelas}" // Set data kelas
        holder.linkTextView.text = mapel.link

        holder.itemView.setOnClickListener {
            showConfirmationDialog(mapel.name, mapel.link)
        }
    }

    private fun showConfirmationDialog(mapelName: String, link: String) {
        AlertDialog.Builder(context).apply {
            setTitle("Konfirmasi")
            setMessage("Apakah Anda yakin ingin mengerjakan soal Ujian \"$mapelName\"?")
            setPositiveButton("Ya") { _, _ ->
                onItemClick(link)
            }
            setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    override fun getItemCount(): Int = mapelList.size
}
