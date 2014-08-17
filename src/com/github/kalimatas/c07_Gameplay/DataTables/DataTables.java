package com.github.kalimatas.c07_Gameplay.DataTables;

import com.github.kalimatas.c07_Gameplay.Aircraft;
import com.github.kalimatas.c07_Gameplay.Textures;

import java.util.ArrayList;

public class DataTables {
    public static ArrayList<AircraftData> initializeAircraftData() {
        ArrayList<AircraftData> data = new ArrayList<>(Aircraft.Type.TYPE_COUNT.ordinal());

        AircraftData eagle = new AircraftData();
        eagle.hitpoints = 100;
        eagle.speed = 200.f;
        eagle.texture = Textures.EAGLE;
        data.add(Aircraft.Type.EAGLE.ordinal(), eagle);

        AircraftData raptor = new AircraftData();
        raptor.hitpoints = 20;
        raptor.speed = 80.f;
        raptor.texture = Textures.RAPTOR;
        raptor.directions.addLast(new Direction(+45.f, 80.f));
        raptor.directions.addLast(new Direction(-45.f, 160.f));
        raptor.directions.addLast(new Direction(+45.f, 80.f));
        data.add(Aircraft.Type.RAPTOR.ordinal(), raptor);

        AircraftData avenger = new AircraftData();
        avenger.hitpoints = 40;
        avenger.speed = 50.f;
        avenger.texture = Textures.AVENGER;
        avenger.directions.addLast(new Direction(+45.f, 50.f));
        avenger.directions.addLast(new Direction(0.f, 50.f));
        avenger.directions.addLast(new Direction(-45.f, 100.f));
        avenger.directions.addLast(new Direction(0.f, 50.f));
        avenger.directions.addLast(new Direction(+45.f, 50.f));
        data.add(Aircraft.Type.AVENGER.ordinal(), avenger);

        return data;
    }
}
