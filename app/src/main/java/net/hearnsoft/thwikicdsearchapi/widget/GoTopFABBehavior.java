package net.hearnsoft.thwikicdsearchapi.widget;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class GoTopFABBehavior extends RecyclerView.OnScrollListener {

    private ExtendedFloatingActionButton top_fab;

    public GoTopFABBehavior(ExtendedFloatingActionButton fab) {
        super();
        this.top_fab = fab;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (dy > 0 && top_fab.getVisibility() != View.VISIBLE){
            top_fab.show();
        } else if (dy < 0 && top_fab.getVisibility() == View.VISIBLE) {
            top_fab.hide();
        }
    }
}
