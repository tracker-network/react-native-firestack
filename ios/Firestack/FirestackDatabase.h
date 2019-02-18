//
//  FirestackDatabase.h
//  Firestack
//
//  Created by Ari Lerner on 8/23/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#ifndef FirestackDatabase_h
#define FirestackDatabase_h

#import "Firebase.h"
#import "RCTEventEmitter.h"
#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#else
#import "RCTBridgeModule.h"
#endif

@interface FirestackDatabase : RCTEventEmitter <RCTBridgeModule> {

}

@property (nonatomic) NSDictionary *_DBHandles;
@property (nonatomic, weak) FIRDatabaseReference *ref;

@end

#endif