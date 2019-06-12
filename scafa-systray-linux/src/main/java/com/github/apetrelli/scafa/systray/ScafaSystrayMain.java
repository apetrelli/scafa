/**
 * Scafa - A universal non-caching proxy for the road warrior
 * Copyright (C) 2015  Antonio Petrelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.apetrelli.scafa.systray;

import org.eclipse.swt.internal.Library;

public class ScafaSystrayMain {

    public static void main(String[] args) {
        Library.loadLibrary("swt");
        Library.loadLibrary("swt-pi3");
        Library.loadLibrary("swt-cairo");
    	ScafaSystrayLauncher launcher = new ScafaSystrayLauncher();
    	launcher.launch();
    }
}
