package cn.refactor.expandablelayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import cn.refactor.library.ExpandableLayout;

/**
 * 作者 : andy
 * 日期 : 15/12/13 18:31
 * 邮箱 : andyxialm@gmail.com
 * 描述 : 测试界面
 */
public class MainActivity extends AppCompatActivity {

    private View mSwitcher;
    private ExpandableLayout mExpandableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwitcher = findViewById(R.id.switcher);
        mExpandableLayout = (ExpandableLayout) findViewById(R.id.expandableLayout);
        mExpandableLayout.setExpand(false);
        mExpandableLayout.setSwitcher(mSwitcher);
        mExpandableLayout.setOnChangeListener(new ExpandableLayout.OnChangeListener() {
            @Override
            public void onPreExpand() {
                Log.d("ExpandableLayout", "onPreExpand");
            }

            @Override
            public void onPreCollapse() {
                Log.d("ExpandableLayout", "onPreCollapse");
            }

            @Override
            public void onExpanded() {
                Log.d("ExpandableLayout", "onExpanded");
            }

            @Override
            public void onCollapsed() {
                Log.d("ExpandableLayout", "onCollapsed");
            }
        });
        findViewById(R.id.btnTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandableLayout.change();
            }
        });

    }
}
