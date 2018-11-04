package fr.litarvan.packetz;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.stick.library.network.ConnectionState;
import org.stick.library.network.Side;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Packet
{
    int id();
    ConnectionState state();
    Side bound();
}
