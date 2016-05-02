/**
* Polling Module 
*
* @author  ADEM F. IDRIZ
* @version 1.0
* @since  2016 - TU Delft
*/

package pollingSystem;

import pal.TECS.*;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import java.awt.BorderLayout;
import java.awt.Dimension;


import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import javax.swing.JButton;
import javax.swing.JPanel;


import java.net.*;
import java.io.*;
import java.util.*;



public class Polling extends JFrame {
	
	Toolkit toolkit;
	Timer timer;
    boolean timeIsUp = false;

	private PALClient thePalClient;
	private JTextArea textWindow=new JTextArea();
	private int message = 0;
	private String PPTcommand = "";
	private int counter = 0;
	private String Title = "";
	private String command = "";
	private String waitsecond = "";
	private String quizURL = "";
	private String RefreshURL= "";
	private int sec =0;
	
	private String resultKey="";

	public Polling(String clientName) {
		
		makeGUI();
		setSize(700, 100);
	    setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
		/*Initiate textWindow */
		setLayout(new BorderLayout());
		setSize(700, 500);
		
		//Put the drawing area in a scroll pane.
        JScrollPane scroller = new JScrollPane(textWindow);
        scroller.setPreferredSize(new Dimension(700,400));
        add(scroller, BorderLayout.SOUTH);
        
		
	    setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setTitle(clientName);
		

		
		/*Create Connection and Subscription */
		connectClientandSubscribe(clientName);
		
	}
	
	  private void connectClientandSubscribe(final String clientName) {

		/*Manually assigned IP */
		String tecsserver = "127.0.0.1";
//		String tecsserver = System.getProperty(TECSSERVER_IP_PROP);
		thePalClient = new PALClient(tecsserver, clientName);
		    
		/*Subscription */
		thePalClient.subscribe(palConstants.SlideControlMsg, palEventHandler_SlideControl);
		
		
		// Start Listening messages 
		thePalClient.startListening();
	  }
	
	 // Handle type of SlideControlMsg 
	  EventHandler<SlideControlCommand> palEventHandler_SlideControl = new EventHandler<SlideControlCommand>() {
	    public void handleEvent(SlideControlCommand event) {
	    	counter++;
	    	
	    	// Parsing Messages
	    	message = ((SlideControlCommand) event).SlideNumber;
	    	
	    	PPTcommand = ((SlideControlCommand) event).command;
	    	
	        int intIndex = PPTcommand.indexOf("&");
	        
	        if(intIndex == - 1){

	           command = PPTcommand;
	           waitsecond ="";
	           quizURL="";
	           
	        }else{
	        	
				String[] partsPPTcommand = PPTcommand.split("&");
				
				command = partsPPTcommand [0];
				waitsecond = partsPPTcommand [1];
				quizURL = partsPPTcommand [2];
				
				sec = Integer.parseInt(waitsecond);
				// Set Timer
				ReminderBeep(sec);
				RefreshURL=quizURL;
	        }
	        
	    	System.out.println("Slide Number: "+ message + "  &  " + "PPTcommand: " + PPTcommand);
			textWindow.append(counter+ "- Slide Number: " + message + "  &  " + "PPTcommand: " + PPTcommand +  System.getProperty("line.separator"));
			textWindow.append("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------" + System.getProperty("line.separator")+ System.getProperty("line.separator"));
		

	    	}
	  	};

			
	  public void ReminderBeep(int seconds) {
	    toolkit = Toolkit.getDefaultToolkit();
	    timer = new Timer();
	    timer.schedule(new RemindTask(), seconds * 1000);
	    
	  }


	  class RemindTask extends TimerTask {
	    public void run() {
	      System.out.println("Time's up!");
	      toolkit.beep();
	      timeIsUp = true;
	      timer.cancel(); 
	      
          // scheduled task 
			try {
				ConnectURL(quizURL);

				/* Poll Title: What's your favorite cupcake?  */
//			ConnectURL("https://www.polleverywhere.com/multiple_choice_polls/sKxNLqlMTJquXoC");
				
				/* Poll Title: The word "robot" is derived from a Slavonic word which means serf. */
//			ConnectURL("https://www.polleverywhere.com/multiple_choice_polls/6xqlxKp4Xgr8LV7");
			
				/* Poll Title: Which of these, according to the definition, is not a robot? */
//			ConnectURL("https://www.polleverywhere.com/multiple_choice_polls/Mo5DyMDlIQNiKvs");
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  
	    }
	  }
		  
	 public void ConnectURL(String argURL)throws Exception {
		 if (timeIsUp) {
			 		timeIsUp = false;
			 		//Check connection to Poll Server 
			 		try {
						InetAddress addr;
						Socket sock = new Socket("www.polleverywhere.com", 80); 
						 addr = sock.getInetAddress();
						 textWindow.append("Connected to " + addr + System.getProperty("line.separator"));
						 sock.close();
						  } catch (IOException e) {
						     System.out.println("Can not connect to ");
						     textWindow.append("Can not connect to " + System.getProperty("line.separator"));
						     System.out.println(e);
						     // Send "noConnection" message to TECS Server
						     resultKey="noConnection";
								thePalClient.send(new PollingData(101, Title +"&"+ resultKey));
								textWindow.append("Sending message with id: 101 PollingData Content:[" + " Poll Title: '" + Title +
										"', Result : '" + resultKey + "']" + System.getProperty("line.separator"));
								textWindow.append("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------" + System.getProperty("line.separator")+ System.getProperty("line.separator"));

						  }
        
			 		//Connect  to poll url
			 		URL quiz = new URL(argURL+"/web.js");
			 
			 		// Open input stream 
			        URLConnection result = quiz.openConnection();
			        BufferedReader in = new BufferedReader(new InputStreamReader(result.getInputStream()));
			        String inputLine;

			        // Keywords 
			        ArrayList Keywords = new ArrayList<String>();
			        // Percentages
			        ArrayList Percentage = new ArrayList<Integer>();
        

			        	
						while ((inputLine = in.readLine()) != null) {

								// find all occurrences forward -"title":"
								for (int intTitle = -1; (intTitle = inputLine.indexOf("\"title\":\"", intTitle + 1)) != -1; ) {
									
								String dummy=inputLine.substring(intTitle+9);
								int quote = dummy.indexOf("\"");				       
								Title = inputLine.substring(intTitle+9,intTitle+9+quote);
								textWindow.append("Poll Title: " + Title +  System.getProperty("line.separator"));
								}
						    
						    	int index_keyword=0;
							    // find all occurrences forward - "keywords":"
						        for (int intKeyword = -1; (intKeyword = inputLine.indexOf("\"keyword\":\"", intKeyword + 1)) != -1; ) {
						        	
						            Keywords.add(index_keyword, inputLine.substring(intKeyword+11,intKeyword+12));
						            index_keyword++;
						        } 

						        int index_Percentage=0;
						        // find all occurrences forward -"percentages":"
						        for (int intPercentage = -1; (intPercentage = inputLine.indexOf("\"results_percentage\":", intPercentage + 1)) != -1; ) {
					            
						            //Only digits
						            String str = inputLine.substring(intPercentage,intPercentage+24).replaceAll("[^0-9]", "");
						            //Convert string into integer
						            int percent = Integer.parseInt(str);
						            Percentage.add(index_Percentage, percent);
						            index_Percentage++;
						        }					
						    
						}
						//	Close input stream 		     
						in.close();
						
						textWindow.append("Available Keywords: "+ Keywords+  System.getProperty("line.separator"));
						textWindow.append("Percentages: "+ Percentage + System.getProperty("line.separator"));

						
						// Find maximum value(s) among Percentage values 						
					    int max=0;
					    
						//Convert Arraylist to Array
					    int[] ret = new int[Percentage.size()];
					    Iterator<Integer> iterator = Percentage.iterator();
					    for (int i = 0; i < ret.length; i++)
					    {
					        ret[i] = iterator.next().intValue();
					        
					        if(ret[i] > max){  
					        	max = ret[i];  
					        } 

					    }
					    

						if (max !=0) {
						
						    // Search Maxima
						    ArrayList dummy = new ArrayList<Integer>(); 
						    int counter=0;
					          for ( int i = 0; i < ret.length; i++)
					          {
					                 if (ret[ i ]  == max)
					                {
					                dummy.add(counter, i);
					                counter++;
					                }
					           }

						
							
						// Convert maximum value(s) of Percentages into Keywords 		
					          StringBuilder stringBuilder = new StringBuilder();
				       
						       
								for (int i = 0; i < dummy.size(); i++) {
								    int value = (Integer) dummy.get(i);
								    if(i>0){stringBuilder.append ("&");}
								    stringBuilder.append ((String) Keywords.get(value));
								}
								
							 resultKey = stringBuilder.toString();
							
							 textWindow.append("Most voted option(s):"+ resultKey + System.getProperty("line.separator"));
							

						
						} else {
						
						// All percentages are ZERO
						resultKey="none";
							
						}
				
						//Send polling message to TECS server				
						thePalClient.send(new PollingData(101, Title +"&"+ resultKey));
						textWindow.append("Sending message with id: 101 PollingData Content:[" + " Poll Title: '" + Title +
								"', Result : '" + resultKey + "']" + System.getProperty("line.separator"));
						textWindow.append("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------" + System.getProperty("line.separator")+ System.getProperty("line.separator"));
						quizURL="";
				}
						



	 		}
		  

		// Create GUI
		private void makeGUI() {

		// Panel
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		// Refresh Button
		JButton btnAnswB = new JButton("Refresh");
		btnAnswB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
			// Refresh connection to latest available URL
			if (RefreshURL != "")
			quizURL=RefreshURL;
			ReminderBeep(1);
			}

			
		});
		btnAnswB.setBounds(570, 10, 100, 30);
		panel.add(btnAnswB);
		
		}
		
		
		// MAIN
		public static void main(String[] args) {
			//creating and showing this application's GUI.
			new Polling ("Polling");
		}
			  
}
