1.controller 处理分发的请求,执行service,返回ModelAndView
2.pojo 数据层(字段-javabean)
3.service spring的主服务--获取数据(利用myBaits动态访问pojo数据层)
4.util 工具包
5.dao 接口供service调用
6.generator 自动生成pojo,dao,xml dao接口的实现即sql语句
  调用过程：controller分发请求--->开启service--->service的是实现类中实例化dao的接口(关联xml与pojo)
  --->动态注入对象,返回数据
  String resource = "mybatis-config.xml";    
  Reader reader = Resources.getResourceAsReader(resource);  
  SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);   
  SqlSession session = sqlSessionFactory.openSession();
  CartMapper mapper = session.getMapper(CartMapper.class);
  mapper.deleteByPrimaryKey(id);调用sql查询
 7.myBaits-plugin  
 8.myBaits-pageHelper myBatis利用sql获取数据时,自动调用获取count等分页参数
 9.用户接口设计
   需求分析：
   
 /************************************************/
 分类管理：设计及封装无限层级的树状数据结构,递归实现