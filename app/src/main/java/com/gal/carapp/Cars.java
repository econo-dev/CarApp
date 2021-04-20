
package com.gal.carapp;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.*;
import com.backendless.geo.GeoPoint;

import java.util.List;
import java.util.Date;

public class Cars
{
  private Date updated;
  private String objectId;
  private String licenseNumber;
  private String name;
  private Date created;
  private String ownerId;
  private String maker;

  public Date getUpdated()
  {
    return updated;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public String getLicenseNumber()
  {
    return licenseNumber;
  }

  public void setLicenseNumber( String licenseNumber )
  {
    this.licenseNumber = licenseNumber;
  }

  public String getName()
  {
    return name;
  }

  public void setName( String name )
  {
    this.name = name;
  }

  public Date getCreated()
  {
    return created;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public String getMaker()
  {
    return maker;
  }

  public void setMaker( String maker )
  {
    this.maker = maker;
  }

                                                    
  public Cars save()
  {
    return Backendless.Data.of( Cars.class ).save( this );
  }

  public void saveAsync( AsyncCallback<Cars> callback )
  {
    Backendless.Data.of( Cars.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( Cars.class ).remove( this );
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( Cars.class ).remove( this, callback );
  }

  public static Cars findById( String id )
  {
    return Backendless.Data.of( Cars.class ).findById( id );
  }

  public static void findByIdAsync( String id, AsyncCallback<Cars> callback )
  {
    Backendless.Data.of( Cars.class ).findById( id, callback );
  }

  public static Cars findFirst()
  {
    return Backendless.Data.of( Cars.class ).findFirst();
  }

  public static void findFirstAsync( AsyncCallback<Cars> callback )
  {
    Backendless.Data.of( Cars.class ).findFirst( callback );
  }

  public static Cars findLast()
  {
    return Backendless.Data.of( Cars.class ).findLast();
  }

  public static void findLastAsync( AsyncCallback<Cars> callback )
  {
    Backendless.Data.of( Cars.class ).findLast( callback );
  }

  public static List<Cars> find( DataQueryBuilder queryBuilder )
  {
    return Backendless.Data.of( Cars.class ).find( queryBuilder );
  }

  public static void findAsync( DataQueryBuilder queryBuilder, AsyncCallback<List<Cars>> callback )
  {
    Backendless.Data.of( Cars.class ).find( queryBuilder, callback );
  }
}