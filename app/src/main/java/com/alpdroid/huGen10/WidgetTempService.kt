package com.alpdroid.huGen10

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.alpdroid.huGen10.ui.WidgetProvider

class WidgetTempService : RemoteViewsService() {


    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ListRemoteViewsFactory(applicationContext)
    }

    class WidgetItem(
        /**
         * Label to display in the list.
         */
        val mLabel: String, val mValue: String, val mValueInt:Int
    )

    class ListRemoteViewsFactory(private val mContext: Context) :
        RemoteViewsFactory {

        private val mWidgetItems: MutableList<WidgetItem> = ArrayList()

        override fun onCreate() {

        }

        override fun onDestroy() {
            mWidgetItems.clear()
        }

        override fun getCount(): Int {
            return mWidgetItems.size
        }

        override fun getViewAt(position: Int): RemoteViews {
            // Position will always range from 0 to getCount() - 1.

            var mValue:String = "Extra"

            // Construct remote views item based on the item xml file and set text based on position.
            val rv = RemoteViews(mContext.packageName, R.layout.widget_temp_item)
            // set ImageView et TexView d'une seule ROW
            // set mValue
            when (position) {
                0 -> {
                    rv.setTextViewText(R.id.widget_temp_item, mWidgetItems[findElement("Internal")].mValue)
                    rv.setImageViewResource(R.id.widget_temp_item_view, R.drawable.internaltemp)
                    mValue=
                        "${mWidgetItems[findElement("Internal")].mLabel}:${mWidgetItems[findElement("Internal")].mValue}"
                }
                1 -> {
                    rv.setTextViewText(R.id.widget_temp_item, mWidgetItems[findElement("External")].mValue)
                    rv.setImageViewResource(R.id.widget_temp_item_view, R.drawable.externaltemp)
                    mValue=mWidgetItems[findElement("External")].mLabel+":"+mWidgetItems[findElement("External")].mValue
                }
                2 -> {
                    rv.setTextViewText(
                        R.id.widget_temp_item,
                        mWidgetItems[findElement("Humidity")].mValue
                    )
                    rv.setImageViewResource(R.id.widget_temp_item_view, R.drawable.humid_clim)
                    mValue=mWidgetItems[findElement("Humidity")].mLabel+":"+mWidgetItems[findElement("Humidity")].mValue
                }

            }

            // Next, we set a fill-intent which will be used to fill-in the pending intent template
            // which is set on the collection view in AppWidgetProvider.
            val fillInIntent =
                Intent().putExtra(WidgetProvider.EXTRA_CLICKED_FILE, mValue)
            rv.setOnClickFillInIntent(R.id.widget_temp_item_layout, fillInIntent)


            // You can do heaving lifting in here, synchronously. For example, if you need to
            // process an image, fetch something from the network, etc., it is ok to do it here,
            // synchronously. A loading view will show up in lieu of the actual contents in the
            // interim.
            return rv
        }

        override fun getLoadingView(): RemoteViews? {
            // You can create a custom loading view (for instance when getViewAt() is slow.) If you
            // return null here, you will get the default loading view.
            return null
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun onDataSetChanged() {


            try{

                    var alpineServices: VehicleServices = AlpdroidApplication.app.alpdroidData


                    mWidgetItems.clear()
                    mWidgetItems.add(
                        WidgetItem(
                            "Internal",
                            String.format(
                                " %d °C",
                                alpineServices.get_internalTemp().toInt())
                            , alpineServices.get_internalTemp().toInt()
                        )
                    )

                    mWidgetItems.add(
                        WidgetItem(
                            "External",
                            String.format(
                                " %d °C",
                                alpineServices.get_MM_ExternalTemp()-40),alpineServices.get_MM_ExternalTemp()-40)
                    )


                    mWidgetItems.add(
                        WidgetItem(
                            "Humidity",
                            String.format(" %d %%", alpineServices.get_IH_humidity().toInt()),alpineServices.get_IH_humidity().toInt())
                        )


                }
                catch (e:Exception)
                {
                    // do nothing


                }


        }


        private fun findElement(element: String): Int {
            for (i in 0 until mWidgetItems.size) {
                if (this.mWidgetItems[i].mLabel == element) {
                    return i
                }
            }
            return -1 // if element not found
        }


    }
  }
