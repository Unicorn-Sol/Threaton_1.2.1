package com.theatron2.uiforthreaton.adapterForAllClasses

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import com.squareup.picasso.Picasso
import com.theatron2.uiforthreaton.Activities.Main2Activity
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.ui.friends.USER_FRIEND_ADMIRERS
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
class CustomSuggestionsAdapterSearchFrag(val context:Context, inflater: LayoutInflater):SuggestionsAdapter<USER_FRIEND_ADMIRERS,CustomSuggestionsAdapterSearchFrag.SuggestionHolder>(inflater) {
    class SuggestionHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    {
        val photo:CircleImageView=itemView.findViewById(R.id.imageViewForSeachMaterialBar)
        val name:TextView=itemView.findViewById(R.id.textViewForSearchMaterial)
        val layout:LinearLayout=itemView.findViewById(R.id.layoutForSearchMaterialBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionHolder {
        val view=LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_for_search_material_bar,parent,false)
        return SuggestionHolder(view)
    }

    override fun getSingleViewHeight(): Int=100

    override fun onBindSuggestionHolder(suggestion: USER_FRIEND_ADMIRERS, p1: SuggestionHolder, p2: Int) {
        p1.name.text=suggestion.name
        Picasso.get().load(suggestion.photo)
            .placeholder(R.drawable.ic_account_circle_black_24dp).into(p1.photo)
        p1.layout.setOnClickListener {
            val intent= Intent(context,
                Main2Activity::class.java)
            intent.putExtra("SuggestId",suggestion.id)
            context.startActivity(intent)
        }
    }



    override fun getFilter(): Filter {

        return object :Filter()
        {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results=FilterResults()
                val term=constraint.toString()
                if(term.isEmpty())
                {
                    suggestions=suggestions_clone
                }
                else
                {
                    suggestions=ArrayList()
                    for(i in suggestions_clone)
                    {
                        if (i.name.toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT))) {
                            suggestions.add(i)
                        }
                    }
                }
                results.values=suggestions
                return results
            }
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                suggestions=results.values as ArrayList<USER_FRIEND_ADMIRERS>
                notifyDataSetChanged()
            }
        }
    }
}