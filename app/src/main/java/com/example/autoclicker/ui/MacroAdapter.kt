package com.example.autoclicker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.autoclicker.R
import com.example.autoclicker.data.Macro
import timber.log.Timber

/**
 * 매크로 리스트를 표시하는 어댑터입니다.
 */
class MacroAdapter(
    private val macros: List<Macro>,
    private val onItemClick: (Macro) -> Unit
) : RecyclerView.Adapter<MacroAdapter.MacroViewHolder>() {

    companion object {
        private const val TAG = "MacroAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MacroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_macro, parent, false)
        return MacroViewHolder(view)
    }

    override fun onBindViewHolder(holder: MacroViewHolder, position: Int) {
        val macro = macros[position]
        holder.bind(macro)
    }

    override fun getItemCount(): Int = macros.size

    /**
     * 매크로 뷰 홀더입니다.
     */
    inner class MacroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivMacroThumbnail: ImageView = itemView.findViewById(R.id.iv_macro_thumbnail)
        private val tvMacroName: TextView = itemView.findViewById(R.id.tv_macro_name)
        private val tvThreshold: TextView = itemView.findViewById(R.id.tv_threshold)
        private val tvClickInterval: TextView = itemView.findViewById(R.id.tv_click_interval)
        private val ivStatus: ImageView = itemView.findViewById(R.id.iv_status)

        fun bind(macro: Macro) {
            try {
                tvMacroName.text = macro.name
                tvThreshold.text = String.format("%.2f", macro.threshold)
                tvClickInterval.text = "${macro.clickInterval}ms"

                // 이미지 설정
                ivMacroThumbnail.setImageBitmap(macro.targetImage)

                // 상태 아이콘 설정
                val statusIcon = if (macro.isActive) {
                    R.drawable.ic_play
                } else {
                    R.drawable.ic_pause
                }
                ivStatus.setImageResource(statusIcon)

                // 클릭 리스너
                itemView.setOnClickListener {
                    Timber.tag(TAG).d("Macro clicked: ${macro.name}")
                    onItemClick(macro)
                }

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error binding macro view holder")
            }
        }
    }
}
