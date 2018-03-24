package com.netspace.teacherpad.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import java.util.ArrayList;

public class HTMLTextView extends View {
    private static Typeface m_TypeFace;
    private Paint m_BackgroundPaint;
    private StaticLayout m_Layout;
    private TextPaint m_Paint;
    private ArrayList<DrawData> m_arrDrawData = new ArrayList();
    private int m_nBottomMargin = 114;
    private int m_nColumnCount = 3;
    private int m_nColumnSpacing = 46;
    private int m_nLeftMargin = 48;
    private int m_nRightMargin = 48;
    private int m_nTopMargin = 114;
    private String m_szText;

    private class DrawData {
        StaticLayout Layout;
        int nX;
        int nY;

        private DrawData() {
        }
    }

    public HTMLTextView(Context context) {
        super(context);
        m_TypeFace = Typeface.SERIF;
        this.m_Paint = new TextPaint();
        this.m_Paint.setColor(-16316665);
        this.m_Paint.bgColor = -1;
        this.m_Paint.setTypeface(m_TypeFace);
        this.m_Paint.setTextSize(18.0f);
        this.m_Paint.setAntiAlias(true);
        this.m_szText = "　　和Larry Page进行面对面交谈时，你会发现他的声音小得有点难以听清。这是14年前的声带神经损伤和去年夏天的感冒造成的，这使他剩下的声带只能十分有限地振动发声。即便如此，Page也没放慢语速，你必须聚精会神地去听。好在他的话总是值得倾听。\n　　40岁的Page所领导的公司是这个星球上最成功、最出名和最古怪的公司之一。Google的第一重身份当然是众所周知的搜索引擎巨无霸，在线广告业务是其收入支柱。但同时，它还有Mibile OS、无人驾驶汽车、可穿戴设备、在线地图、可再生资源和提供网络的热气球等五花八门的业务。这家公司的发展策略就是：赚大钱的主流业务+为未来铺路的激进计划。\n　　Page喜欢把Google形容为一家有着\"登月（moonshot）\"野心的公司。\"我不是建议把所有的钱都投给那些冒险性项目上，\"他在Google总部接受采访时说，\"但我们必须抽出一般公司用来研发产品的等量资源，花在那些有着长期影响和更大野心，并且超出普通人想象的事儿上，比如登月之类的事儿。\"\n　　Google现在就正准备着一个彻头彻尾的\"登月计划\"。他们正在筹备的Calico是一家以医疗健康与延缓衰老为主攻方向的公司。生化科技先驱Genentech的前任执行总裁Arthur Levinson也向这家完全独立的公司注入了资金并出任CEO一职。有着生物化学博士学位的Levinson仍然保留他在苹果以及Genentech的董事会主席职位。换句话说，Google这回和死亡杠上了。\n　　在这方面，Google 曾有过失败的尝试 &ndash; Google Health，一个个人医疗记录服务。但对Calico，Google要为它制定比大部分医保企业更为长远的计划。在接受时代周刊的专访时，Page说：\"在很多行业里，一个想法从提出到付诸现实需要10年甚至20年时间。医疗绝对属于这样的行业。我们必须聚焦在最有意义的事情上，然后花上这么多年来实现它们。\"\n　　硅谷不会有第二家公司敢做出这番宏论。小公司没钱，大公司无志。苹果的产品发布也许称得上有够炫目，但是隔几年只推出一两款产品的路线只能被大部分人视作是一种短期规划。相反，Google的路数却能每每让人惊呼，\"这不会是真的吧？\"上个星期苹果发布了一款金色iPhone；而Google呢？不好意思，他们成立了一个有朝一日可能战胜死神的公司。\n　　于是，这样一个问题被摆上了台面：为什么一个建立在搜索和广告上的信息公司要不惜付出一笔可观的资源向人类生存的基本法则 &ndash; 衰老和死亡发起挑战？又要由谁去完成这项挑战呢？\n　　未完待续...";
        this.m_BackgroundPaint = new Paint();
        this.m_BackgroundPaint.setColor(-1);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), this.m_BackgroundPaint);
        if (this.m_arrDrawData.size() == 0) {
            String szText = this.m_szText;
            int nColumnWidth = (((getWidth() - this.m_nLeftMargin) - this.m_nRightMargin) - (this.m_nColumnSpacing * (this.m_nColumnCount - 1))) / this.m_nColumnCount;
            int nColumnHeight = (getHeight() - this.m_nTopMargin) - this.m_nBottomMargin;
            int nXPos = this.m_nLeftMargin;
            int nYPos = this.m_nTopMargin;
            String szCurrentText = "";
            while (!szText.isEmpty()) {
                StaticLayout Layout = new StaticLayout(szText, this.m_Paint, nColumnWidth, Alignment.ALIGN_NORMAL, 1.58f, 0.0f, true);
                int nlastVisibleLineNumber = Layout.getLineForVertical(nColumnHeight);
                if (Layout.getLineBaseline(nlastVisibleLineNumber) > nColumnHeight) {
                    nlastVisibleLineNumber--;
                }
                int nEnd = Layout.getLineEnd(nlastVisibleLineNumber);
                if (szText.length() > nEnd) {
                    szCurrentText = szText.substring(0, nEnd);
                } else {
                    szCurrentText = szText;
                }
                HTMLTextView hTMLTextView = this;
                DrawData drawData = new DrawData();
                drawData.Layout = new StaticLayout(szCurrentText, this.m_Paint, nColumnWidth, Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
                drawData.nX = nXPos;
                drawData.nY = nYPos;
                this.m_arrDrawData.add(drawData);
                if (szText.length() <= nEnd) {
                    break;
                }
                szText = szText.substring(nEnd);
                nXPos += this.m_nColumnSpacing + nColumnWidth;
                if (nXPos + nColumnWidth > getWidth()) {
                    break;
                }
            }
        }
        for (int i = 0; i < this.m_arrDrawData.size(); i++) {
            DrawData DrawData = (DrawData) this.m_arrDrawData.get(i);
            canvas.save();
            canvas.translate((float) DrawData.nX, (float) DrawData.nY);
            DrawData.Layout.draw(canvas);
            canvas.restore();
        }
    }
}
