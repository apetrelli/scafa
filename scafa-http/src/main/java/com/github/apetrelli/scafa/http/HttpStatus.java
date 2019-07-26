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
package com.github.apetrelli.scafa.http;

public enum HttpStatus {

	IDLE, REQUEST_LINE, REQUEST_LINE_CR, REQUEST_LINE_LF, POSSIBLE_HEADER, POSSIBLE_HEADER_CR, SEND_HEADER_AND_END,
	POSSIBLE_HEADER_LF, HEADER, HEADER_CR, HEADER_LF, BODY, PENULTIMATE_BYTE, LAST_BYTE, CHUNK_COUNT, CHUNK_COUNT_CR,
	CHUNK_COUNT_LF, CHUNK, CHUNK_CR, CHUNK_LF, CONNECT;
}
