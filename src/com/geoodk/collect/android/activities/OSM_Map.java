package com.geoodk.collect.android.activities;

/*
 * 06.30.2014
 * Jon Nordling
 * Matias Something?
 * 
 * This activity is to map the data offline
 * 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;










import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import javax.xml.parsers.ParserConfigurationException;






import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;


import javax.xml.transform.dom.DOMSource;


import javax.xml.transform.stream.StreamResult;



//import org.apache.james.mime4j.util.StringArrayMap;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;



import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.geoodk.collect.android.R;
import com.geoodk.collect.android.R.id;
import com.geoodk.collect.android.R.layout;
import com.geoodk.collect.android.R.menu;
import com.geoodk.collect.android.database.ODKSQLiteOpenHelper;
import com.geoodk.collect.android.provider.InstanceProviderAPI;
import com.geoodk.collect.android.provider.FormsProviderAPI.FormsColumns;
import com.geoodk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import com.geoodk.collect.android.spatial.CustomMarkerHelper;
import com.geoodk.collect.android.spatial.XmlGeopointHelper;
import com.geoodk.collect.android.spatial.CustomPopupMaker;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Build;

public class OSM_Map extends Activity {
	private MapView mapView;
	private MapController myMapController;
	//private ItemizedIconOverlay<OverlayItem> complete_overlays;
	//private ItemizedIconOverlay<OverlayItem> final_overlays;
	//private ItemizedIconOverlay<OverlayItem> defalt_overlays;
	private DefaultResourceProxyImpl resource_proxy;
	private Context self = this;
	private Marker loc_marker;  //This is the marker used to display the user's location
	private Criteria criteria = new Criteria(); // ?? Not sure what a criteria is but probably should find out!
	private String provider; //  Gps or Network providor
	//public XmlGeopointHelper geoheler = new XmlGeopointHelper();

	public Location lastLocation;
	private static final String t = "Map";
	//ArrayList<OverlayItem> marker_list = new ArrayList<OverlayItem>();
	private List<String[]> markerListArray = new ArrayList<String[]>();
	private LocationManager locationManager;
	
	//This section is used to know the order of a array of instance data in the db cursor
	public static final int pos_url=0;
	public static final int pos_id=1;
	public static final int pos_name=2;
	public static final int pos_status=3;
	public static final int pos_uri=4;
	public static final int pos_geoField=5;
	
	//This is used to store temp latitude values
	private Double lat_temp;
	private Double lng_temp;
	
	//Keep Track if GPS button is on or off
	
	public Boolean gpsStatus = false;
	
	
	XmlPullParserFactory factory;
	
	
    //This function comes after the onCreate function
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//myMapController.setZoom(4);
	}
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.osmmap_layout); //Setting Content to layout xml
		setTitle(getString(R.string.app_name) + " > Mapping"); // Setting title of the action
		
		//Layout Code MapView Connection and options
		mapView = (MapView)findViewById(R.id.MapViewId);
		mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
		mapView.setMultiTouchControls(true);
		mapView.setBuiltInZoomControls(true);
		mapView.setUseDataConnection(true);
		
		//Figure this out!!!!! I want to call this a a class and return the some value!!!!!!1
		//String name = geoheler.getGeopointDBField(temp); 
        
        //Sets the  Resource Proxy
        resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
		
        final ImageButton gps_button = (ImageButton)findViewById(R.id.gps_button);
        //This is the gps button and its functionality
        gps_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//Toast.makeText(self, "Works", Toast.LENGTH_LONG).show();
                // Perform action on click
           	 //locationManager.requestLocationUpdates(1000, 1, criteria, myLocationListener);
            	//gps_button.setBackground(R.drawable.ic_menu_mylocation_blue);
            	//gps_button.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_menu_mylocation_blue));
            	if(gpsStatus ==false){
            		gps_button.setImageResource(R.drawable.ic_menu_mylocation_blue);
            		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, myLocationListener);
            		gpsStatus = true;
            	}else{
            		gps_button.setImageResource(R.drawable.ic_menu_mylocation);
            		locationManager.removeUpdates(myLocationListener);
            		gpsStatus = false;
            	}
            	
            	
            	
            	 //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, LocationListener);
            }
        });
        
		//Toast.makeText(this,"Resume", Toast.LENGTH_SHORT).show();
		//mapView.getOverlays().clear();
		//mapView.invalidate();
      //The locationManager 
        
  		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
  		lastLocation 	= locationManager.getLastKnownLocation(	LocationManager.GPS_PROVIDER);
        loc_marker = new Marker(mapView);
        updateMyLocation();
        

		
		
		
	}


	@Override
	protected void onResume() {
		//Initializing all the
		super.onResume(); // Find out what this does? bar
		hideInfoWindows();
		mapView.getOverlays().clear();
		updateMyLocation();
		mapView.invalidate();

        //Spinner s = new Spinner(this);
        
        String selection = InstanceColumns.STATUS + " != ?"; // Find out what this does
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};  //Look like if arguments passed idk.
        
        //For each instance in the db if there is a point then add it to the overlay/marker list
        String sortOrder = InstanceColumns.STATUS + " DESC, " + InstanceColumns.DISPLAY_NAME + " ASC";
        Cursor instance_cur = getContentResolver().query(InstanceColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);
        //todo catch when c==null
        instance_cur.moveToFirst();
        while (!instance_cur.isAfterLast()) {
        	String instance_url = (String) instance_cur.getString(instance_cur.getColumnIndex("instanceFilePath"));
        	String instance_form_id = (String) instance_cur.getString(instance_cur.getColumnIndex("jrFormId"));
        	String instance_form_name = (String) instance_cur.getString(instance_cur.getColumnIndex("displayName"));
        	String instance_form_status = (String) instance_cur.getString(instance_cur.getColumnIndex("status"));
            Uri instanceUri = ContentUris.withAppendedId(InstanceColumns.CONTENT_URI, instance_cur.getLong(instance_cur.getColumnIndex(InstanceColumns._ID)));
            String instanceUriString = instanceUri.toString();
            String geopoint_field = null;

			try {
				geopoint_field = (String)getGeoField(instance_form_id);
				//Toast.makeText(this,geopoint_field, Toast.LENGTH_SHORT).show();
			} catch (XmlPullParserException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String[] markerObj = {instance_url,instance_form_id,instance_form_name,instance_form_status,instanceUriString,geopoint_field};
            markerListArray.add(markerObj);
            
            //startActivity(new Intent(Intent.ACTION_EDIT, instanceUri));
            
        	//Determine the geoPoint Field
        	try {
				createMaker(markerObj);
				//addGeoPointMarkerList(instance_cur);
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			instance_cur.moveToNext();
		}
        
        instance_cur.close();
        
        
        //This is used to wait a second to wait the center the map on the points
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		  @Override
		  public void run() {
		    //Do something after 100ms
			  GeoPoint point;
			  int zoom;
			  if(lastLocation != null){
				point = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()); 
				zoom = 9;
			  }else{
				  point = new GeoPoint(34.08145, -39.85007);
				  zoom = 3;
			  }
			  	mapView.getController().setZoom(zoom);
				mapView.getController().setCenter(point);
		  }
		}, 100);
        //set_marker_overlay_listners();
        
        //mapView.getOverlays().add(defalt_overlays);
        //mapView.invalidate();
		mapView.invalidate();
	}
	
	private void updateMyLocation() {
		// TODO Auto-generated method stub
		
		if(lastLocation != null){
			//Set the location of marker on the map
			//Toast.makeText(this,lastLocation.getLatitude()+" "+lastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
			GeoPoint loc = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()); 
			loc_marker.setPosition(loc);
			loc_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
			loc_marker.setIcon(getResources().getDrawable(R.drawable.loc_logo_small));
			mapView.getOverlays().add(loc_marker);
        }
		
	}



	public void hideInfoWindows(){
		List<Overlay> overlays = mapView.getOverlays();
		for (Overlay overlay : overlays) {
			if (overlay.getClass() == CustomMarkerHelper.class){
				((CustomMarkerHelper)overlay).hideInfoWindow();
			}
		}
		
	}
	 public void createMaker (String[] cur_mark) throws XmlPullParserException, IOException {
		 
		 	//Read the Xml file of the instance 
	         XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	         factory.setNamespaceAware(true);
	         XmlPullParser xpp = factory.newPullParser();
	         xpp.setInput(new FileReader(new File(cur_mark[pos_url])));
	         int eventType = xpp.getEventType();
	         
	         //For each of the objects in the instance xml <location>
	         while (eventType != XmlPullParser.END_DOCUMENT) {
	        	 if (xpp.getName()!=null){
	        		if(xpp.getName().equals(cur_mark[pos_geoField])){
	        			if (eventType == XmlPullParser.START_TAG){
	        				String tagname = xpp.getName();
	        				eventType = xpp.next();
	        				String value = xpp.getText();
	        				if (value != null){
	        					//marker_list.add(instance);
	        					String[] location = xpp.getText().split(" ");
		        				Double lat = Double.parseDouble(location[0]);
		        				Double lng = Double.parseDouble(location[1]);
		        				GeoPoint point = new GeoPoint(lat, lng); 
		        				CustomMarkerHelper startMarker = new CustomMarkerHelper(mapView);
		        				startMarker.setMarker_name(cur_mark[pos_name]);
		        				startMarker.setMarker_uri(Uri.parse(cur_mark[pos_uri]));
		        				startMarker.setMarker_status(cur_mark[pos_status]);
		        				startMarker.setMarker_url(cur_mark[pos_url]);
		        				startMarker.setMarker_id(cur_mark[pos_id]);
		        				startMarker.setMarker_geoField(cur_mark[pos_geoField]);
		        				startMarker.setPosition(point);
		        				startMarker.setIcon(getResources().getDrawable(R.drawable.map_marker));
		        				startMarker.setTitle("Name: "+ cur_mark[pos_name]);
		        				startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		        				startMarker.setSnippet("Status: "+cur_mark[pos_status]);
		        				startMarker.setDraggable(true);
		        				startMarker.setOnMarkerDragListener(draglistner);
		        				startMarker.setInfoWindow(new CustomPopupMaker(mapView, Uri.parse(cur_mark[pos_uri])));
		        				
		        				//popup_button.setOnClickListener(new on);
		        				//startMarker.setSubDescription("Desc");

		        				//startMarker.setIcon(getResources().getDrawable(R.drawable.pin_marker));
		        				mapView.getOverlays().add(startMarker);

		        				//OverlayItem overlayitem = new OverlayItem("Title", "SampleDescription", point);
		        				//Drawable marker = this.getResources().getDrawable(R.drawable.d);
		        				//overlayitem.setMarker(marker);
		        				//marker_list.add(overlayitem);
		        				break;
	        				}else{
	        					break;
	        				}
	        			}
	        		}

				}
	        	 eventType = xpp.next();
	         }
	 }
	//Make this more eficient so that you dont have to use the cursor all the time only if the form has not be queried 
	 public String getGeoField(String form_id) throws XmlPullParserException, IOException{
		String formFilePath ="";
		String formsortOrder = FormsColumns.DISPLAY_NAME + " ASC, " + FormsColumns.JR_VERSION + " DESC";
        Cursor form_curser =  getContentResolver().query(FormsColumns.CONTENT_URI, null, null, null, formsortOrder);  
        form_curser.moveToFirst();
        //int count = 0;
        while(!form_curser.isAfterLast()){
        	 String tempformID = form_curser.getString(form_curser.getColumnIndex("jrFormId"));
        	 if(tempformID.equals(form_id)){
        		 //read xml and get geopoint table name
        		 //Toast.makeText(this,form_id+" == "+tempformID, Toast.LENGTH_SHORT).show();
        		 formFilePath =form_curser.getString(form_curser.getColumnIndex("formFilePath"));
        		 break;
        		 //Read the 
        		 //count++;
        	 }else{
        		 //Toast.makeText(this,form_id+" !: "+tempformID, Toast.LENGTH_SHORT).show();
        	 }
        	 form_curser.moveToNext();
        }
        form_curser.close();
        String db_field_name= "";
        if (formFilePath != ""){
        	//That file exists
		 	//Read the Xml file of the instance 
	         factory = XmlPullParserFactory.newInstance();
	         factory.setNamespaceAware(true);
	         XmlPullParser xpp = factory.newPullParser();
	         xpp.setInput(new FileReader(new File(formFilePath)));
	         int eventType = xpp.getEventType();
	         
	         while (eventType != XmlPullParser.END_DOCUMENT) {
	        	 if (xpp.getName()!=null){
	        		if(xpp.getName().equals("bind")){
	        			String bind_type = xpp.getAttributeValue(null, "type");
	        			String[] bind_nodeset = (xpp.getAttributeValue(null, "nodeset")).split("/");
	        			String bind_db_name = bind_nodeset[bind_nodeset.length -1];
	        			//Toast.makeText(this,bind_type+" "+bind_db_name, Toast.LENGTH_SHORT).show();
	        			if (bind_type.equals("geopoint")){
	        				db_field_name= bind_db_name;
	        				//Toast.makeText(this,bind_type+" "+db_field_name, Toast.LENGTH_SHORT).show();
	        				break;
	        			}
	        		}
	        	 }
	        	 eventType = xpp.next();
	        	 
	         }
	         
	         
	         //Now you loop through the xml form to find the geopoint.
	         //Im sure ODK has something that figured this out, but I could not find it so I wrote it
	         
	         
        }else{
        	//File file Does not exist
        }
		return db_field_name;
        
        
        

	 }
	 


	 //This is going to be the listner for the devices locations

	    private LocationListener myLocationListener = new LocationListener(){

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				//updateLoc(location);
				mapView.getOverlays().remove(loc_marker);
				//Toast.makeText(OSM_Map.this,"Location Update", Toast.LENGTH_LONG).show();
				GeoPoint current_loc = new GeoPoint(location);
				loc_marker.setPosition(current_loc);
				mapView.getOverlays().add(loc_marker);
				mapView.invalidate();

				//loc_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
				//loc_marker.setIcon(getResources().getDrawable(R.drawable.loc_logo_small));
				//mapView.getOverlays().add(loc_marker);
				//Toast.makeText(this,(location.getLatitude())+" "+location.getLongitude(), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub

			}

	    };
	    private OnMarkerDragListener draglistner = new OnMarkerDragListener(){

			@Override
			public void onMarkerDrag(Marker m) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onMarkerDragEnd(Marker m) {
				GeoPoint newlocation = m.getPosition();
				newlocation.getLatitude();
				newlocation.getLongitude();
				loc_marker = new Marker(mapView);
				String lat = Double.toString(((CustomMarkerHelper)m).getPosition().getLatitude());
				String lng = Double.toString(((CustomMarkerHelper)m).getPosition().getLongitude());
				//Toast.makeText(OSM_Map.this,lat+" "+lng, Toast.LENGTH_LONG).show();
				askToChangePoint(m);
				// TODO Auto-generated method stub
				//Toast.makeText(OSM_Map.this,((CustomMarkerHelper)m).getMarker_url(), Toast.LENGTH_LONG).show();
			}

			@Override
			public void onMarkerDragStart(Marker m) {
				// TODO Auto-generated method stub
				//lat_temp =  Double.toString(((CustomMarkerHelper)m).getPosition().getLatitude());
				//lng_temp  =  Double.toString(((CustomMarkerHelper)m).getPosition().getLongitude());
				lat_temp =  ((CustomMarkerHelper)m).getPosition().getLatitude();
				lng_temp  =  ((CustomMarkerHelper)m).getPosition().getLongitude();
				//Toast.makeText(OSM_Map.this,lat+" "+lng, Toast.LENGTH_LONG).show();
				
			}
	    	
	    };
	    
	    protected void askToChangePoint(Marker m) {
	    	final Marker mk = m;
	    	//final Double lat = ((CustomMarkerHelper)m).getPosition().getLatitude();
	    	//final Double lng = ((CustomMarkerHelper)m).getPosition().getLongitude();
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                    //loadPublicLegends(mainActivity);
								
								try {
									changeInstanceLocation(mk);
								} catch (XmlPullParserException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ParserConfigurationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (SAXException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (TransformerException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						
					
                                    break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                    // Cancel button clicked
                            		((CustomMarkerHelper)mk).setPosition(new GeoPoint(lat_temp, lng_temp));
                            		mapView.invalidate();
                                    break;
                            }
                    }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(OSM_Map.this);
            builder.setMessage(
                    "Are you sure you want to change the location of this point?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("Cancel", dialogClickListener).show();
    }
		 public void changeInstanceLocation(Marker mk) throws XmlPullParserException, IOException, ParserConfigurationException, SAXException, TransformerException{
			 String url = ((CustomMarkerHelper)mk).getMarker_url();
			 File xmlFile = new File(url);
			 DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			 DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			 Document doc = docBuilder.parse(xmlFile);
			 Node file_value = doc.getElementsByTagName(((CustomMarkerHelper)mk).getMarker_geoField()).item(0).getFirstChild();
			 String temp = Double.toString((((CustomMarkerHelper)mk).getPosition().getLatitude()))+ " "+ Double.toString((((CustomMarkerHelper)mk).getPosition().getLongitude()))+ " 0.1 0.1";
			 String old_loc = Double.toString(lat_temp) +" " +Double.toString(lng_temp); 
			 file_value.setNodeValue(temp);
			 //String old_loc = Double.toString(lat_temp) +" " +Double.toString(lng_temp);
			 TransformerFactory transformerFactory = TransformerFactory.newInstance();
			 Transformer transformer = transformerFactory.newTransformer();
			 DOMSource source = new DOMSource(doc);
			 StreamResult results = new StreamResult(xmlFile);
			 transformer.setOutputProperty(OutputKeys.INDENT,"yes");
			 transformer.transform(source, results);
			 
			 mapView.invalidate();
			 
			 
			 //Node node = doc.getFirstChild();
			 //NamedNodeMap nodeAttributes = node.getAttributes();
			 //Node x = nodeAttributes.getNamedItem("location");
			 //NamedNodeMap temp = (NamedNodeMap) node.getChildNodes();
			 //Node temp = nodeAttributes.getNamedItem(((CustomMarkerHelper)mk).getMarker_geoField());
			 //Node temp =nodeAttributes.getNamedItemNS(null, ((CustomMarkerHelper)mk).getMarker_geoField());
			 
			 //Toast.makeText(OSM_Map.this,url, Toast.LENGTH_LONG).show();
			 
			 //Save the new location of the marker
			 
			 	//Read the Xml file of the instance 
	        /*XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	         factory.setNamespaceAware(true);
	         XmlPullParser xpp = factory.newPullParser();
	         xpp.setInput(new FileReader(new File(url)));
	         int eventType = xpp.getEventType();
	         
	         //For each of the objects in the instance xml <location>
	         while (eventType != XmlPullParser.END_DOCUMENT) {
	        	 if (xpp.getName()!=null){
	        		if(xpp.getName().equals(((CustomMarkerHelper)mk).getMarker_geoField())){
	        			if (eventType == XmlPullParser.START_TAG){
	        				String tagname = xpp.getName();
	        				eventType = xpp.next();
	        				String value = xpp.getText();
	        				if (value != null){
	        					//marker_list.add(instance);
	        					String[] location = xpp.getText().split(" ");
	        					Toast.makeText(OSM_Map.this,location[0]+" "+location[1], Toast.LENGTH_LONG).show();
	        					//I need to write the new lat/lng to the xml File 
	        					//Help!!!!!!!111
	        					
	        				}
	        			}
	        		}
	        	 }
	        	 eventType = xpp.next();
	         }*/
				
		 }
		 
		 @Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				super.onCreateContextMenu(menu, v, menuInfo);
				  MenuInflater inflater = getMenuInflater();
				  inflater.inflate(R.menu.map_click_menu, menu);
			}
			@Override
		    public boolean onCreateOptionsMenu(Menu menu) {
		        MenuInflater inflater = getMenuInflater();
		        inflater.inflate(R.layout.map_menu_layout, menu);
		        return true;
		    }
			@Override
			public boolean onOptionsItemSelected(MenuItem item) {
				// TODO Auto-generated method stub
				return super.onOptionsItemSelected(item);
			}

		    
}