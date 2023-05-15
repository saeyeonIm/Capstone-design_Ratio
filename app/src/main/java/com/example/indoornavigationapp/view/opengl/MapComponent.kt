package com.example.indoornavigationapp.view.opengl

class MapComponent(name: String, drawOrder: ShortArray, color: FloatArray, floor: Int, coordinates: Array<FloatArray>) {
    var name:String = name
    var drawOrder: ShortArray = drawOrder
    var color: FloatArray = color
    var floor: Int = floor
    var coordinates: Array<FloatArray> = coordinates

    override fun toString(): String {
        return "MapComponent(name='$name', drawOrder=${drawOrder.contentToString()}, color=${color.contentToString()}, floor=$floor, coordinates=${coordinates.contentToString()})"
    }

}