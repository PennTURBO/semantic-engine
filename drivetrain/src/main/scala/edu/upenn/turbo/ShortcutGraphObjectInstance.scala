package edu.upenn.turbo

trait ShortcutGraphObjectInstance extends GraphObjectInstance with ShortcutGraphObjectSingleton
{
    var namedGraph: String
    var instantiation: String
}