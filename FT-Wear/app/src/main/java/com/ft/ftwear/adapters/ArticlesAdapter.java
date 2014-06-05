package com.ft.ftwear.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ft.ftwear.R;
import com.ft.ftwear.models.ArticleModel;

import java.util.List;

/**
 * Created by albertoelias on 05/06/2014.
 */
public class ArticlesAdapter  extends BaseAdapter {

    private final LayoutInflater mInflater;
    private List<ArticleModel> mList;
    private Context mContext;

    public ArticlesAdapter(Context context, List items) {
        mContext = context;
        mList = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int pos) {
        if (mList != null) {
            return mList.get(pos);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class ViewHolder {
        TextView title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View view;
        ArticleModel article = (ArticleModel)getItem(position);

        if (convertView == null) {
            view = mInflater.inflate(R.layout.articlelist_row, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView)view.findViewById(R.id.title);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        holder.title.setText(article.getTitle());
        return view;
    }
}
