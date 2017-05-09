package proyecto_dns;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class dnsclient {
	
	private static void dumpA(PrintStream ps,AResourceRecord r){
		ps.println(r.getAddress().getHostAddress());
	}
	
	private static void dumpAAAA(PrintStream ps, AAAAResourceRecord r){
		ps.println(r.getAddress().getHostAddress());
	}
	
	private static void dumpNS(PrintStream ps, NSResourceRecord r){
		ps.println(r.getNS());
	}
	public static void pintar_respuesta(PrintStream ps, ResourceRecord r,InetAddress server_anterior){
		ps.print("A: " +server_anterior+" "+r.getRRType()+" "+ r.getTTL()+" ");
		switch(r.getRRType()){
		case A:
			dumpA(ps, (AResourceRecord)r);
			break;
		case AAAA:
			dumpAAAA(ps, (AAAAResourceRecord)r);
			break;
		case NS:
			dumpNS(ps, (NSResourceRecord)r);
			break;
		default :
			ps.println("que queires hacer con el "+ r.getRRType()+" record ");
		
		}
		
		
		
	}
	public static void dump(PrintStream ps, ResourceRecord r){
		ps.print("TTL: " + r.getTTL()+" "+r.getDomain()+" "+r.getRRType()+"-->");
		switch(r.getRRType()){
		case A:
			dumpA(ps, (AResourceRecord)r);
			break;
		case AAAA:
			dumpAAAA(ps, (AAAAResourceRecord)r);
			break;
		case NS:
			dumpNS(ps, (NSResourceRecord)r);
			break;
		default :
			ps.println("que queires hacer con el "+ r.getRRType()+" record ");
		
		}
		
	}

/**
 * @param args the command line arguments
 * @throws java.net.UnknownHostException
 * @thorws java.net.SocketException
 */

	public static void main (String[] args) throws UnknownHostException, SocketException, Exception {
		
		//creamos un mensaje para gaurdar la consulta que enviaremos al servidor
		Message question = null;
		System.out.println("solo se implementa udp");
		/*System.out.println("indique el tipo y direccion:");
		Scanner teclado=new Scanner(System.in);*/
		Scanner entrada=null;
		String tipo_direccion=null;//teclado.nextLine();
		InetAddress server_anterior=InetAddress.getByName(args[1]);
		Message respuesta1=null;
		
		try {
			entrada = new Scanner(new FileInputStream("fichero.txt"));
		}

		catch (FileNotFoundException e) {
			System.out.println("Fichero de consultas inexistente: ");
			System.exit(-1);
		}
		
		
		do{
			tipo_direccion=entrada.nextLine();
		//seleccionamos el tipo de consulta
		switch(tipo_direccion.trim().split("\\ ")[0]){
		
		case "A":
			question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.A, false);
			break;
		case "NS":
			question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.NS, false);
			break;
		case "AAAA":
			question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.AAAA, false);
			break;
		default:
			System.out.println("\nTIPO NO VALIDO\n");
			break;
		}
		
		DatagramSocket s = new DatagramSocket();
		byte[] mensaje = question.toByteArray();
		s.send(new DatagramPacket (mensaje, mensaje.length, InetAddress.getByName(args[1]), 53));
		
		System.out.println("Q: udp "+args[1]+"  "+tipo_direccion.trim().split("\\ ")[0]+"  "+tipo_direccion.trim().split("\\ ")[1]);
		
		byte[] respuesta_en_bytes = new byte [1500];
		DatagramPacket answer = new DatagramPacket (respuesta_en_bytes, respuesta_en_bytes.length);		
		s.receive(answer);
		
		
		//convierto los bytes de answer a algo que pueda interpretar
		Message respuesta = new Message (respuesta_en_bytes);	
		
		/*System.out.println("Answer:");
		respuesta.getAnswers().forEach((rr) -> {
			dump(System.out,rr);
			
		});
		System.out.println("NS:");
		respuesta.getNameServers().forEach((rr) -> {
			dump(System.out,rr);
			
		});
		System.out.println("Additional:");
		respuesta.getAdditonalRecords().forEach((rr) -> {
			dump(System.out,rr);
			
		});*/
		
		//sirver para realizar las consultas sucesivas en busca de la respuesta
		int fin=1;
		int num=0;
		do{
			if(respuesta.getAnswers().isEmpty()){
	//RECORRO LOS NAME SERVERS Y LOS ADDITIONAL Y SEGUN EL TIPO DEL NAME SERVER LE REFORMULO DE NUEVO LA CONSULTA PERO CON EL RRType ORIGINAL
			if(!respuesta.getNameServers().isEmpty()){
				for(int i=0;i<num+1;i++){
					int coincide=0;
					for(int j=0;j<respuesta.getAdditonalRecords().size();j++){
						if(respuesta.getAnswers().size()!=0 && tipo_direccion.trim().split("\\ ")[0].equals("NS")){
							
						}
						else{
						if(((NSResourceRecord)respuesta.getNameServers().get(num)).getNS().toString().equals(respuesta.getAdditonalRecords().get(j).getDomain().toString())){
						coincide=1;
						switch(respuesta.getAdditonalRecords().get(j).getRRType().toString()){
						
						case "A":
							System.out.println("A: "+server_anterior+"  "+((NSResourceRecord)respuesta.getNameServers().get(num)).getRRType()+"  "+((NSResourceRecord)respuesta.getNameServers().get(num)).getTTL()+"  "+((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getDomain());
							System.out.println("A: "+server_anterior+"  "+((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getRRType()+"  "+((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getTTL()+"  "+((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress());

							if(tipo_direccion.trim().split("\\ ")[0].equals("A")){
								question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.A, false);
								mensaje = question.toByteArray();
								
								if(respuesta.getAdditonalRecords().get(j).getRRType().equals(RRType.AAAA)){
									System.out.println("Q: udp "+InetAddress.getByName(((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.A+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
									else{
									System.out.println("Q: udp "+InetAddress.getByName(((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.A+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
								
								
							}
							
							else if(tipo_direccion.trim().split("\\ ")[0].equals("NS")){
								System.out.println("el tipo de consulta es :"+respuesta.getAdditonalRecords().get(j).getRRType().toString());
								question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.NS, false);
								mensaje = question.toByteArray();
								
									if(respuesta.getAdditonalRecords().get(j).getRRType().equals(RRType.AAAA)){
									System.out.println("Q: udp "+InetAddress.getByName(((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.NS+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
									else{
									System.out.println("Q: udp "+InetAddress.getByName(((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.NS+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
								
								
								
							}
							
							else{
								question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.AAAA, false);
								mensaje = question.toByteArray();
								
								if(respuesta.getAdditonalRecords().get(j).getRRType().equals(RRType.AAAA)){
									System.out.println("Q: udp "+InetAddress.getByName(((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.AAAA+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
									else{
									System.out.println("Q: udp "+InetAddress.getByName(((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.AAAA+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
								
								
								
							}
							
							break;
						
						case "NS":
							System.out.println("A: "+server_anterior+"  "+((NSResourceRecord)respuesta.getNameServers().get(num)).getRRType()+"  "+((NSResourceRecord)respuesta.getNameServers().get(num)).getTTL()+"  "+((NSResourceRecord)respuesta.getAdditonalRecords().get(j)).getDomain());
							System.out.println("A: "+server_anterior+"  "+((NSResourceRecord)respuesta.getAdditonalRecords().get(j)).getRRType()+"  "+((NSResourceRecord)respuesta.getAdditonalRecords().get(j)).getTTL()+"  ");//+((NSResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress());
							
							if(tipo_direccion.trim().split("\\ ")[0].equals("A")){
								question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.A, false);
								mensaje = question.toByteArray();
								
								if(respuesta.getAdditonalRecords().get(j).getRRType().equals(RRType.AAAA)){
									System.out.println("Q: udp "+InetAddress.getByName(((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.A+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
									else{
									System.out.println("Q: udp "+InetAddress.getByName(((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.A+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
								
								
								
							}
							
							else if(tipo_direccion.trim().split("\\ ")[0].equals("NS")){
								question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.NS, false);
								mensaje = question.toByteArray();
								
								if(respuesta.getAdditonalRecords().get(j).getRRType().equals(RRType.AAAA)){
									System.out.println("Q: udp "+InetAddress.getByName(((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.NS+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
									else{
									System.out.println("Q: udp "+InetAddress.getByName(((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.NS+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
								
																
							}
							
							else{
								question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.AAAA, false);
								mensaje = question.toByteArray();
								
								if(respuesta.getAdditonalRecords().get(j).getRRType().equals(RRType.AAAA)){
									System.out.println("Q: udp "+InetAddress.getByName(((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.AAAA+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
									else{
									System.out.println("Q: udp "+InetAddress.getByName(((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.AAAA+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
							
								
							}
							break;
						
						case "AAAA":
							System.out.println("A: "+server_anterior+"  "+((NSResourceRecord)respuesta.getNameServers().get(num)).getRRType()+"  "+((NSResourceRecord)respuesta.getNameServers().get(num)).getTTL()+"  "+((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getDomain());
							System.out.println("A: "+server_anterior+"  "+((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getRRType()+"  "+((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getTTL()+"  "+((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress());
								
							if(tipo_direccion.trim().split("\\ ")[0].equals("A")){
								question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.A, false);
								mensaje = question.toByteArray();
								
								if(respuesta.getAdditonalRecords().get(j).getRRType().equals(RRType.AAAA)){
									System.out.println("Q: udp "+InetAddress.getByName(((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.A+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
									else{
									System.out.println("Q: udp "+InetAddress.getByName(((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.A+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
								
								
								
							}
							
							else if(tipo_direccion.trim().split("\\ ")[0].equals("NS")){
								question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.NS, false);
								mensaje = question.toByteArray();
								
								if(respuesta.getAdditonalRecords().get(j).getRRType().equals(RRType.AAAA)){
								System.out.println("Q: udp "+InetAddress.getByName(((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.NS+"  "+tipo_direccion.trim().split("\\ ")[1]);
								s.send(new DatagramPacket (mensaje, mensaje.length,((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
								}
								else{
								System.out.println("Q: udp "+InetAddress.getByName(((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.NS+"  "+tipo_direccion.trim().split("\\ ")[1]);
								s.send(new DatagramPacket (mensaje, mensaje.length,((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
								}
								
								
								
							}
							
							else{
								question = new Message (tipo_direccion.trim().split("\\ ")[1], RRType.AAAA, false);
								mensaje = question.toByteArray();
								
								if(respuesta.getAdditonalRecords().get(j).getRRType().equals(RRType.AAAA)){
									System.out.println("Q: udp "+InetAddress.getByName(((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.AAAA+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AAAAResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));	
								}
								else{
									System.out.println("Q: udp "+InetAddress.getByName(((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress().toString().replace("/", ""))+"  "+RRType.AAAA+"  "+tipo_direccion.trim().split("\\ ")[1]);
									s.send(new DatagramPacket (mensaje, mensaje.length,((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress() , 53));
									}
								
								
							}
							break;
							
						default:
							System.out.println("tipo no valido");
							break;
						}
						
						 respuesta_en_bytes = new byte [1500];
						 answer = new DatagramPacket (respuesta_en_bytes, respuesta_en_bytes.length);		
						 s.receive(answer);
						 
				//convierto los bytes de answer a algo que pueda interpretar
						 respuesta1 = new Message (respuesta_en_bytes);
						 if(respuesta1.getAnswers().isEmpty()){
							 if(respuesta1.getNameServers().isEmpty()){
								 if(num+1<respuesta.getNameServers().size()){
									 num++;
								 }
								 else{
									 System.out.println("No hay respuesta");
									 System.exit(-1);
								 }
							 }
							 else{
								 server_anterior=((AResourceRecord)respuesta.getAdditonalRecords().get(j)).getAddress();
								 respuesta1=respuesta;
								 respuesta=new Message(respuesta_en_bytes);
								 //muestro las partes del nuevo mensaje
								/* System.out.println("Answer:");
								 respuesta.getAnswers().forEach((rr) -> {
									 dump(System.out,rr);
										
								 });
								 System.out.println("NS:");
								 respuesta.getNameServers().forEach((rr) -> {
									 dump(System.out,rr);
										
								 });
								 System.out.println("Additional:");
								 respuesta.getAdditonalRecords().forEach((rr) -> {
									 dump(System.out,rr);
										
								 });*/
							 }
						 }
						 
						 else{
							 respuesta1=respuesta;
							 respuesta=new Message(respuesta_en_bytes);
							 //muestro las partes del nuevo mensaje
							/* System.out.println("Answer:");
							 respuesta.getAnswers().forEach((rr) -> {
								 dump(System.out,rr);
									
							 });
							 System.out.println("NS:");
							 respuesta.getNameServers().forEach((rr) -> {
								 dump(System.out,rr);
									
							 });
							 System.out.println("Additional:");
							 respuesta.getAdditonalRecords().forEach((rr) -> {
								 dump(System.out,rr);
									
							 });*/
							
						 }
						 
					}
						}
					
				}//fin del bucle de additional
					//compruebo si no hay direccion para algun name server
					if(coincide==0){
						System.out.println("No hay registro tipo A en sección ADDITIONAL para "+((NSResourceRecord)respuesta.getNameServers().get(num)).getNS()+"\n");						
						if(num+1<respuesta.getNameServers().size()){
							 num++;
						 }
						 else{
							 fin=0;
						 }
					}
				}//fin del bucle para name servers
				
			
			}//fin del if para recorrer los servidores ns
			}//fin del if para consulta sin answer
			
			//REPRESENTO LA RESPUESTA O EL CNAME EN CASO DE DARSE
			else{
				for(int i=0;i<respuesta.getAnswers().size();i++){
				
						if(respuesta.getAnswers().get(i).getRRType().equals(RRType.CNAME)){
							System.out.println("A  "+server_anterior+" CNAME");
			
							if(i+1==respuesta.getAnswers().size()){
								fin=0;
							}
						}
						else{
							pintar_respuesta(System.out,respuesta.getAnswers().get(i),server_anterior);
							fin=0;
						}	
					}
				
			}
			
			
			}while(fin!=0);
		
		}while(entrada.hasNextLine());
	}

}




