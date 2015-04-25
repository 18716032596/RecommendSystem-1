package org.juefan.alg.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.juefan.IO.FileIO;
import org.juefan.alg.LFM;
import org.juefan.alg.LFM.State;
import org.juefan.alg.UserCF;
import org.juefan.data.RatingData;
import org.juefan.eva.Evaluation;

public class TestLFM {

	public static Set<Integer> user = new HashSet<Integer>();
	public static Set<Integer> item = new HashSet<Integer>();
	public static List<Integer> itemList = new ArrayList<Integer>();
	public static Map<Integer, Integer> map = new HashMap<Integer, Integer>();

	public static Map<Integer, Map<Integer, Float>>  UserItemTrain = new HashMap<Integer, Map<Integer, Float>> ();
	public static Map<Integer, Map<Integer, Float>>  UserItemTest = new HashMap<Integer, Map<Integer, Float>> ();
	public static LFM lfm = new LFM();
	
	public static List<Float> randList = new ArrayList<Float>();
	
	public static void setRandList(){
		/*List<State> tList = new ArrayList<LFM.State>();
		int Total = 0;
		float ltotal = 0;
		for(int k: map.keySet()){
			tList.add(lfm.new State(k, map.get(k)));
			Total += map.get(k);
		}
		
		Collections.sort(tList, LFM.compare);
		for(State s: tList){
			ltotal += s.sim;
			randList.add(ltotal/Total);
			itemList.add(s.TemID);
		}*/
		for(int k: map.keySet()){
			//for(int i = 0; i < map.get(k)/10; i++){
				itemList.add(k);
			//}
		}
		System.out.println("itemList = " + itemList.size());
	}
	
	public static Map<Integer, Float> getFu(Map<Integer, Float> item){
		Map<Integer, Float> map = new HashMap<Integer, Float>();

		/*int rand = 0;
		while(map.size() < item.size() * 2 && item.size() + map.size() < itemList.size()){
			float R = (float) Math.random();
			boolean bool = true;
			for(int i = 0; i < randList.size() && bool; i++){
				if(randList.get(i) >= R){
					rand = i;
					bool = false;
				}
			}
			rand = (int) (Math.random() * randList.size());
			if(!item.containsKey(itemList.get(rand))){
				//System.out.println(itemList.get(rand) + "\t" + TestLFM.map.get(itemList.get(rand)));
				map.put(itemList.get(rand), (float) 0);
			}
		}*/
		
		while(map.size() < item.size()*2 && item.size() + map.size() < TestLFM.item.size() * 0.8){
			int rand = (int) (Math.random() * itemList.size());
			if(!item.containsKey(itemList.get(rand))){
				//System.out.println(itemList.get(rand) + "\t" + TestLFM.map.get(itemList.get(rand)));
				map.put(itemList.get(rand), (float) 0);
			}
		}
		//System.out.println(map);
		return map;
	}

	public static Set<Integer> MapToSet(Map<Integer, Float> item){
		Set<Integer> tSet = new HashSet<Integer>();
		for(int k: item.keySet())
			tSet.add(k);
		return tSet;
	}

	public static void main(String[] args) {
		FileIO fileIO = new FileIO();
		fileIO.SetfileName(System.getProperty("user.dir") + "\\data\\input\\ml-1m\\ratings.dat");
		fileIO.FileRead();
		List<String> list = fileIO.cloneList();

		for(String s:list){
			RatingData data = new RatingData(s);
			float rand = (float) Math.random();
			if(rand >= (float)1/8){
				if(UserItemTrain.containsKey(data.userID)){
					UserItemTrain.get(data.userID).put(data.movieID, (float) 1);
				}else {
					Map<Integer, Float> tMap = new HashMap<Integer, Float>();
					tMap.put(data.movieID, (float) 1);
					UserItemTrain.put(data.userID, tMap);
				}
				if(map.containsKey(data.movieID)){
					map.put(data.movieID, map.get(data.movieID) + 1);
				}else {
					map.put(data.movieID, 1);
				}
				user.add(data.userID);
				item.add(data.movieID);
			}else {
				if(UserItemTest.containsKey(data.userID)){
					UserItemTest.get(data.userID).put(data.movieID, (float) 1);
				}else {
					Map<Integer, Float> tMap = new HashMap<Integer, Float>();
					tMap.put(data.movieID, (float) 1);
					UserItemTest.put(data.userID, tMap);
				}
			}		
		}
		/*for(int set:map.keySet())
			itemList.add(set);*/
		/**���ɸ�����*/
		setRandList();
		System.out.println("���ڹ������̶�");
		int Fu = 0;
		for(int user: UserItemTrain.keySet()){
			UserItemTrain.get(user).putAll(getFu(UserItemTrain.get(user)));
			if(++Fu % 100 == 0)
				System.out.println("�ѹ��� " + Fu +" ���������û�����");
		}
		System.out.println("�������������");

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm");//�������ڸ�ʽ
		String dataString = "\\data\\output\\Result\\" + df.format(new Date()) + "_result.txt";
		LFM lfm = new LFM(user, item);
		for(int trac = 0; trac <= 100; trac++){
			LFM.LatentFactorModel(UserItemTrain);
			for(int user:UserItemTrain.keySet()){
				if(UserItemTest.containsKey(user)){
					Evaluation.setEvaluation(MapToSet(UserItemTest.get(user)), lfm.getResysList(user, UserItemTrain.get(user)));
				}
			}
			System.out.println("׼ȷ�� = " + Evaluation.getPrecision() * 100 + "%\t\t�ٻ��� = " + Evaluation.getRecall() * 100 + "%");
			FileIO.FileWrite(System.getProperty("user.dir") + dataString, "===================ʹ���㷨 : " + lfm.toString()
					+ "=====================\n�������: "			
					+ "\nlatent = " + LFM.latent
					+"\nalpha = " + LFM.alpha
					+"\nlambda = " + LFM.lambda
					+ "\n׼ȷ�� = " + Evaluation.getPrecision() * 100 + "%\n�ٻ��� = " + Evaluation.getRecall() * 100 + "%\n", true);
		}
	}

}
