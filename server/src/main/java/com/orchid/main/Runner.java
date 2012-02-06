package com.orchid.main;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.orchid.net.server.NetworkServerModule;
import com.orchid.net.server.main.NetworkServer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * User: Igor Petruk
 * Date: 25.12.11
 * Time: 8:37
 */

/*
class Grapher {
  public static void graph(String filename, Injector demoInjector) throws IOException {
    PrintWriter out = new PrintWriter(new File(filename), "UTF-8");

    Injector injector = Guice.createInjector(new GrapherModule(), new GraphvizModule());
    GraphvizRenderer renderer = injector.getInstance(GraphvizRenderer.class);
    renderer.setOut(out).setRankdir("TB");

    injector.getInstance(InjectorGrapher.class)
        .of(demoInjector)
        .graph();
  }
}
*/

public class Runner {
    public static void main(String[] argv) {
        Injector injector = Guice.createInjector(new NetworkServerModule());
        NetworkServer server = injector.getInstance(NetworkServer.class);
        server.start();
    }
}
